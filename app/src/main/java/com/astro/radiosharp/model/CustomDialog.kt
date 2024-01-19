import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
 import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.astro.radiosharp.R
import com.astro.radiosharp.databinding.CustomAlertDialogBinding

class CustomDialog(context: Context, activity: Activity) {

    private val binding: CustomAlertDialogBinding

    private val dialog: AlertDialog

    private val _viewModelObserver = MutableLiveData<() -> Unit>()
    val viewModelObserver: LiveData<() -> Unit>
        get() = _viewModelObserver

    var doINeedExitApp = false

    init {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.custom_alert_dialog,
            null,
            false
        )

        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()


        binding.answerYes.setOnClickListener {
            setExitApp(doINeedExitApp,activity)
            viewModelObserver.value?.invoke()
            dialog.dismiss()
        }

        binding.answerNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun setExitApp(boolean : Boolean, activity: Activity) {
        if (boolean) {
            activity.finish()
        } else {
            dialog.dismiss()
        }
     }

    fun setIcon(id: Int) {
        binding.iconAlertDialog.setImageResource(id)
    }

    fun setAnswerYesAction(action: () -> Unit) {
        _viewModelObserver.value = action
    }

    fun setTextDialog(text : String) {
        binding.titleTextDialog.text = text
    }

    fun showDialog() {
        dialog.show()
    }
}

