package com.idsanna.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class IdsannaService : Service(), RecognitionListener, TextToSpeech.OnInitListener {
    companion object { const val ACTION_LISTEN = "com.idsanna.android.LISTEN"; const val ACTION_STOP = "com.idsanna.android.STOP" }
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private lateinit var voiceSession: VoiceSession

    override fun onCreate() {
        super.onCreate()
        voiceSession = VoiceSession(this)
        createChannel()
        tts = TextToSpeech(this, this)
        startForeground(7, notification("Servicio activo; esperando instrucción"))
        recoverPersistedOperations()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) { ACTION_LISTEN -> listenOnce(); ACTION_STOP -> stopSelf() }
        return START_STICKY
    }

    private fun recoverPersistedOperations() {
        val results = RecoveryCoordinator(OperationStore(this)).recover()
        RecoveryStateIntegrator(RuntimeStateStore(this)).apply(results)
        if (results.isEmpty()) return

        val summary = results.groupingBy { it.decision.action }.eachCount()
            .entries.joinToString(", ") { "${it.key.name.lowercase(Locale.ROOT)}=${it.value}" }
        update("Recuperación revisada: $summary")
    }

    private fun createChannel() { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("idsanna", "IDsanna", NotificationManager.IMPORTANCE_LOW)) }
    private fun notification(text: String): Notification = NotificationCompat.Builder(this, "idsanna").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("IDsanna").setContentText(text).setOngoing(true).build()
    private fun update(text: String) { getSystemService(NotificationManager::class.java).notify(7, notification(text)) }
    private fun listenOnce() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { update("Reconocimiento de voz no disponible"); speak("El reconocimiento de voz no está disponible"); return }
        recognizer?.destroy(); recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { it.setRecognitionListener(this) }
        val request = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con IDsanna") }
        update("Escuchando; di una instrucción"); recognizer?.startListening(request)
    }
    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "idsanna-response") }
    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty()
        if (text.isEmpty()) { update("No se entendió la instrucción"); speak("No pude entender la instrucción"); return }
        if (!voiceSession.isActive()) {
            if (voiceSession.tryActivate(text)) { update("Sesión de voz activa por cinco minutos"); speak("Sesión activada") }
            else { update("Sesión bloqueada; requiere código"); speak("Di el código de activación para iniciar una sesión") }
            return
        }
        update("Instrucción recibida"); speak("Recibí: $text")
    }
    override fun onError(error: Int) { update("Escucha finalizada; código $error") }
    override fun onReadyForSpeech(params: Bundle?) { update("Micrófono listo") }
    override fun onBeginningOfSpeech() { update("Escuchando") }
    override fun onEndOfSpeech() { update("Procesando voz") }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() }
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { recognizer?.destroy(); tts?.stop(); tts?.shutdown(); super.onDestroy() }
}
