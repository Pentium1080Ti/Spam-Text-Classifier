package com.pentium1080ti.spamtextclassifier

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import org.json.JSONObject
import java.io.IOException

@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Classifier(private var context: Context, private var filename: String) {

    private var callback: DataCallback? = null;
    private var maxlen: Int? = null;
    private var vocab: HashMap<String, Int>? = null;

    fun setCallback(callback: DataCallback) {
        this.callback = callback;
    }

    fun setVocab(data: HashMap<String, Int>?) {
        this.vocab = data;
    }

    fun setMaxLength(maxlen: Int) {
        this.maxlen = maxlen;
    }

    fun loadData() {
        val loadVocabTask = LoadVocabularyTask(callback);
        loadVocabTask.execute(loadJSONFromAsset(filename));
    }

    private fun loadJSONFromAsset(filename: String): String? {
        var json: String? = null;
        try {
            val inputStream = context.assets.open(filename);
            val size = inputStream.available();
            val buffer = ByteArray(size);
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    fun tokenize(message: String): IntArray {
        val parts: List<String> = message.split(" ")
        val tokenizedMessage = ArrayList<Int>()
        for (part in parts) {
            if (part.trim() != "") {
                var index: Int? = 0;
                index = if (vocab!![part] == null) {
                    0;
                } else {
                    vocab!![part]
                }
                tokenizedMessage.add(index!!)
            }
        }
        return tokenizedMessage.toIntArray();
    }

    fun pad(intArray: IntArray): IntArray {
        val maxlen = this.maxlen;
        return when {
            intArray.size > maxlen!! -> {
                intArray.sliceArray(0..maxlen);
            }
            intArray.size < maxlen -> {
                val array = ArrayList<Int>();
                array.addAll(intArray.asList());
                for (x in array.size until maxlen) {
                    array.add(0);
                }
                array.toIntArray();
            }
            else -> {
                intArray;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class LoadVocabularyTask(private var callback: DataCallback?) :
        AsyncTask<String, Void, HashMap<String, Int>?>() {

        override fun doInBackground(vararg params: String?): HashMap<String, Int>? {
            val jsonObject = JSONObject(params[0]);
            val iterator: Iterator<String> = jsonObject.keys()
            val data = HashMap<String, Int>()
            while (iterator.hasNext()) {
                data[iterator.next()] = jsonObject.get(iterator.next()) as Int;
            }
            return data;
        }

        override fun onPostExecute(result: HashMap<String, Int>?) {
            super.onPostExecute(result);
            callback?.onDataProcessed(result);
        }

    }

}