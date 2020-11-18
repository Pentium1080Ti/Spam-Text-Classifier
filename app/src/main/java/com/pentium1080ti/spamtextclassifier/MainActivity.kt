package com.pentium1080ti.spamtextclassifier

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.collections.HashMap

class MainActivity : Activity() {

    private var message : EditText? = null;
    private var classify : Button? = null;
    private var resultText : TextView? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        classify = findViewById(R.id.classify);
        resultText = findViewById(R.id.result);

        classify?.setOnClickListener(View.OnClickListener {
            val classifier = Classifier(this, "word_dict.json");
            classifier.setMaxLength(171);
            classifier.setCallback(object : DataCallback {
                @SuppressLint("SetTextI18n")
                override fun onDataProcessed(result: HashMap<String, Int>?) {
                    val text = message?.text.toString().toLowerCase(Locale.ROOT).trim();
                    if(!TextUtils.isEmpty(text)) {
                        classifier.setVocab(result);
                        val tokenizedMessage = classifier.tokenize(text);
                        val paddedMessage = classifier.pad(tokenizedMessage);
                        val results = classifySequence(paddedMessage);
                        val class1 = results[0];
                        val class2 = results[1];
                        resultText?.text = String.format("SPAM: %.2f%%", class2*100);
                    } else {
                        Toast.makeText(this@MainActivity, "Enter a message", Toast.LENGTH_LONG).show();
                    }
                }
            });
            classifier.loadData();
        });
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val MODEL_ASSETS_PATH = "model.tflite";
        val assetFileDescriptor = assets.openFd(MODEL_ASSETS_PATH);
        return FileInputStream(assets.openFd(MODEL_ASSETS_PATH).fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength);
    }

    fun classifySequence(intArray: IntArray): FloatArray {
        val interpreter = Interpreter(loadModelFile());
        val inputs: Array<FloatArray> = arrayOf(intArray.map { it.toFloat() }.toFloatArray());
        val outputs : Array<FloatArray> = arrayOf(floatArrayOf(0.0f, 0.0f));
        interpreter.run(inputs, outputs);
        return outputs[0];
    }

}