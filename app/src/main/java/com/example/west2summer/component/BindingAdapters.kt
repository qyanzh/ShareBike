package com.example.west2summer.component

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.west2summer.R

@BindingAdapter("glide")
fun ImageView.glide(url: String?) {
    if (url != null) {
        this.minimumHeight = 0
        Glide.with(this).load(url).centerCrop().into(this)
    } else {
        this.minimumHeight = this.resources.getDimension(R.dimen.alpha_image_min_height).toInt()
    }
}

@BindingAdapter("glide_avatar")
fun ImageView.glideAvatar(url: String?) {
    if (url != null) {
        Glide.with(this).load(url).centerCrop().into(this)
    } else {
        Glide.with(this).load(R.drawable.image_android_q).centerCrop().into(this)
    }
}
