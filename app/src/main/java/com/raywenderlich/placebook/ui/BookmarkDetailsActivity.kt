package com.raywenderlich.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.util.ImageUtils
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import java.io.File
import java.util.Observer

class BookmarkDetailsActivity : AppCompatActivity(), PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

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
            androidx.lifecycle.Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
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
        photoFile = null
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException) {
            return
        }
        photoFile?.let { photoFile ->
// 5
            val photoUri = FileProvider.getUriForFile(this,
                "com.raywenderlich.placebook.fileprovider",
                photoFile)
// 6
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
// 8
            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                .forEach { grantUriPermission(it, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
// 9
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }

    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun updateImage(image: Bitmap) {
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
// 1
        if (resultCode == android.app.Activity.RESULT_OK) {
// 2
            when (requestCode) {
// 3
                REQUEST_CAPTURE_IMAGE -> {
// 4
                    val photoFile = photoFile ?: return
// 5
                    val uri = FileProvider.getUriForFile(this,
                        "com.raywenderlich.placebook.fileprovider",
                        photoFile)
                    revokeUriPermission(uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
// 6
                    val image = getImageWithPath(photoFile.absolutePath)
                    image?.let { updateImage(it) }
                }

                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null)
                {
                    val imageUri = data.data as Uri
                    val image = getImageWithAuthority(imageUri)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(uri,
            resources.getDimensionPixelSize(
                R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                R.dimen.default_image_height),
            this)
    }

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }



}