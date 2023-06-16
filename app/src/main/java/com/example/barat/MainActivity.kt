package com.example.barat


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.barat.ml.Modelsign
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class MainActivity : AppCompatActivity() {

    lateinit var bitmap:Bitmap
    lateinit var imgview:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Barat)
        setContentView(R.layout.activity_main)

        imgview = findViewById(R.id.iv_picture)

        val fileName = "label.txt"
        val inputString = application.assets.open(fileName).bufferedReader().use{ it.readText() }
        var townList = inputString.split("\n")

        var tv:TextView = findViewById(R.id.textView)

        var select: Button = findViewById(R.id.btn_take)

        select.setOnClickListener(View.OnClickListener {
            var intent:Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 100)
        })

        var predict:Button = findViewById(R.id.btn_translate)
        predict.setOnClickListener(View.OnClickListener {

            var resized: Bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true)

            val model = Modelsign.newInstance(this)

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)

            var tbuffer = TensorImage.fromBitmap(resized)
            var byteBuffer = tbuffer.buffer

            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val max = getMax(outputFeature0.floatArray)

            tv.setText(townList[max])

            model.close()

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imgview.setImageURI(data?.data)

        var uri: Uri?= data?.data

        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)


    }

    fun getMax(arr:FloatArray): Int{

        var ind = 0
        var min = 0.0f

        for(i in 0..100)
        {
            if(arr[i]>min)
            {
                ind = i
                min = arr[i]
            }
        }
        return ind
    }
}






