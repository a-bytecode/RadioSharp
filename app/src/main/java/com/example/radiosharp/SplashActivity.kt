package com.example.radiosharp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.constraintlayout.motion.widget.MotionLayout

class SplashActivity : AppCompatActivity() {

    private lateinit var motionLayout : MotionLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        motionLayout = findViewById(R.id.myMotionLayout_splash)
        motionLayout.startLayoutAnimation() // Hier wird die Animation gestartet.


        motionLayout.setTransitionListener(object :MotionLayout.TransitionListener{ // Trigger sind Ereignisse die während der Animation
            // ausgelöst werden können.
            override fun onTransitionStarted( // Hier könnte man beim Start der Animation Aktivitäten ausführen.
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange( // Hier könnte man während der Animaton Aktivitäten starten oder Aktionen Ausführen.
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                // Zielen sind Optional...
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) { // Sobald der Splash aufhört zu laden
                // wird hier die Mainactivity gestartet
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                finish()

            }

            override fun onTransitionTrigger( // Mann könnte am Ende der Animation die Auslösung eines Triggers verwenden,
                // um bestimmte Elemente im Layout zu animieren, andere Aktivitäten zu starten oder andere Aktionen auszuführen,
                // die für meine App wichtig sind.
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
                // Zeilen sind Optional...
            }
        } )
    }
}