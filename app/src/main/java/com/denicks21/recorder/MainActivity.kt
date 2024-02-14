package com.denicks21.recorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var startTV: Button
    lateinit var pauseTV: Button
    lateinit var stopTV: Button
    lateinit var playTV: Button
    lateinit var statusTV: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var mFileName: String? = null
    private var isRecording = false
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        pauseTV = findViewById(R.id.btnPause)
        stopTV = findViewById(R.id.btnStop)
        playTV = findViewById(R.id.btnPlay)

        val buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation)
        startTV.startAnimation(buttonAnimation)
        pauseTV.startAnimation(buttonAnimation)
        stopTV.startAnimation(buttonAnimation)
        playTV.startAnimation(buttonAnimation)

        startTV.setOnClickListener {
            if (!isRecording) {
                startRecording()
            }
        }

        pauseTV.setOnClickListener {
            if (isRecording) {
                pauseRecording()
            } else if (isPlaying) {
                pausePlaying()
            }
        }

        stopTV.setOnClickListener {
            stopRecording()
            stopPlaying()
        }

        playTV.setOnClickListener {
            if (!isRecording && !isPlaying) {
                playAudio()
            }
        }
    }

    private fun startRecording() {
        if (checkPermissions()) {
            val fileName = "${externalCacheDir?.absolutePath}/Record.3gp"
            mRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(fileName)
                try {
                    prepare()
                    start()
                    statusTV.text = "Recording in progress"
                    isRecording = true
                    mFileName = fileName
                } catch (e: IOException) {
                    Log.e("MainActivity", "prepare() failed")
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun pauseRecording() {
        mRecorder?.apply {
            pause()
            statusTV.text = "Recording paused"
            isRecording = false
        }
    }

    private fun stopRecording() {
        mRecorder?.apply {
            stop()
            release()
            statusTV.text = "Recording stopped"
            isRecording = false
        }
        mRecorder = null
    }

    private fun playAudio() {
        mPlayer = MediaPlayer().apply {
            try {
                setDataSource(mFileName)
                prepare()
                start()
                statusTV.text = "Playing recording"
                this@MainActivity.isPlaying = true
            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }
        }
    }

    private fun pausePlaying() {
        mPlayer?.apply {
            pause()
            statusTV.text = "Playback paused"
            this@MainActivity.isPlaying = false
        }
    }

    private fun stopPlaying() {
        mPlayer?.apply {
            stop()
            statusTV.text = "Playback stopped"
            this@MainActivity.isPlaying = false
        }
    }

    private fun checkPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(this, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE), REQUEST_AUDIO_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
            if (permissionToRecord && permissionToStore) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}
