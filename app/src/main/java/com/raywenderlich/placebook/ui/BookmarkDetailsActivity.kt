package com.raywenderlich.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import com.raywenderlich.placebook.R
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
    }
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }
}