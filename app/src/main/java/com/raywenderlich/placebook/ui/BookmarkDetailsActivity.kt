package com.raywenderlich.placebook.ui

import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity : AppCompatActivity(), PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun populateFields() {
        bookmarkDetailsView?.let { bookmarkView ->
            editTextName.setText(bookmarkView.name)
            editTextPhone.setText(bookmarkView.phone)
            editTextNotes.setText(bookmarkView.notes)
            editTextAddress.setText(bookmarkView.address)
        }
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
            }
        }
        imageViewPlace.setOnClickListener {
            replaceImage()
        }
    }

    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this,
            Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
                it?.let {
                    bookmarkDetailsView = it
                    populateFields()
                    populateImageView()
                }
            })
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    private fun saveChanges() {
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = editTextName.text.toString()
            bookmarkView.notes = editTextNotes.text.toString()
            bookmarkView.address = editTextAddress.text.toString()
            bookmarkView.phone = editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCaptureClick() {
        Toast.makeText(this, "Camera Capture", Toast.LENGTH_SHORT).show()
    }

    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick", Toast.LENGTH_SHORT).show()
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }


}