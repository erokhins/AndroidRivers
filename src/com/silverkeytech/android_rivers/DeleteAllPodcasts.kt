package com.silverkeytech.android_rivers

import android.os.AsyncTask
import org.holoeverywhere.app.Activity
import android.content.Context
import android.util.Log
import java.io.File
import com.silverkeytech.android_rivers.db.removePodcast
import com.silverkeytech.android_rivers.db.SortingOrder
import com.silverkeytech.android_rivers.db.getPodcastsFromDb

public class DeleteAllPodcasts(it: Context?): AsyncTask<String, Int, Result<Int>>(){
    class object {
        public val TAG: String = javaClass<DeleteAllPodcasts>().getSimpleName()
    }

    val context: Activity = it!! as Activity
    val dialog: InfinityProgressDialog = InfinityProgressDialog(context, context.getString(R.string.deleting_all_podcasts)!!)

    //Prepare stuff before execution
    protected override fun onPreExecute() {
        dialog.onCancel{
            dlg ->
            dlg.dismiss()
            this@DeleteAllPodcasts.cancel(true)
        }

        dialog.show()
    }

    var rawCallback: ((Result<Int>) -> Unit)? = null

    public fun executeOnComplete(callback: (Result<Int>) -> Unit): DeleteAllPodcasts {
        rawCallback = callback
        return this
    }

    //Download river data in a thread
    protected override fun doInBackground(vararg p0: String?): Result<Int>? {
        var deletedPodcasts = 0
        //get all active podcasts
        val podcasts = getPodcastsFromDb(SortingOrder.DESC)
        if (podcasts.size == 0)
            return Result.right(deletedPodcasts)

        try{
            for(current in podcasts){
                var f = File(current.localPath)

                if (f.exists())
                    f.delete()

                val res = removePodcast(current.id)
                if (res.isFalse()){
                    Log.d(TAG, "Fail in deleting file ${f.name} ${res.exception?.getMessage()}")
                    break
                }
                else
                    deletedPodcasts++
            }
        }
        catch(e : Exception){
            Log.d(TAG, "Fail in trying to delete a file ${e.getMessage()}")
        }
        finally{
            return Result.right(deletedPodcasts)
        }
    }

    protected override fun onPostExecute(result: Result<Int>?) {
        dialog.dismiss()

        if (rawCallback != null)
            rawCallback!!(result!!)
    }
}