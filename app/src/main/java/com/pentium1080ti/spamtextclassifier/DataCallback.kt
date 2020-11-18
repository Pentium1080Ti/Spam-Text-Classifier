package com.pentium1080ti.spamtextclassifier

public interface DataCallback {

    fun onDataProcessed(result: HashMap<String, Int>?);

}