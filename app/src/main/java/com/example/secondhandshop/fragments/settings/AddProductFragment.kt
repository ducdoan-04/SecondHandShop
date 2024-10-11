package com.example.secondhandshop.fragments.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.databinding.FragmentAddProductBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.AddProductViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddProductViewModel>()

    private var selectedImages = mutableListOf<Uri>()
    private var selectedColors = mutableListOf<Int>()
    private val productsStorage = Firebase.storage.reference
    private val firestore = Firebase.firestore
    private val categories = arrayOf("Chair", "Cupboard", "Table", "Accessory", "Furniture","Best deals","Special Products")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbar.visibility = View.GONE
                        Glide.with(requireView()).load(it.data!!.imagePath).error(
                            ColorDrawable(Color.BLACK)
                        ).into(binding.imageUser)
                        binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                        binding.tvUserId.text = "2handshop.vn/${it.data.email}"
                        binding.tvViewShop.text = "${it.data.email}"
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbar.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }

        binding.buttonColorPicker.setOnClickListener {
            ColorPickerDialog.Builder(requireContext())
                .setTitle("Product color")
                .setPositiveButton("Select", object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                        envelope?.let {
                            selectedColors.add(it.color)
                            updateColors()
                        }
                    }
                })
                .setNegativeButton("Cancel") { colorPicker, _ ->
                    colorPicker.dismiss()
                }.show()
        }

        val selectImagesActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intent = result.data

                // Multiple images selected
                if (intent?.clipData != null) {
                    val count = intent.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        val imageUri = intent.clipData?.getItemAt(i)?.uri
                        imageUri?.let { selectedImages.add(it) }
                    }
                } else {
                    val imageUri = intent?.data
                    imageUri?.let { selectedImages.add(it) }
                }
                updateImage()
            }
        }

        binding.buttonImagesPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "image/*"
            }
            selectImagesActivityResult.launch(intent)
        }

        binding.buttonSaveProduct.setOnClickListener {
            if (!validateInformation()) {
                Toast.makeText(requireContext(), "Check your inputs", Toast.LENGTH_SHORT).show()
            } else {
                saveProduct()
            }
        }

        setHasOptionsMenu(true)
    }



    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                // Do something with the selected category
                Toast.makeText(requireContext(), "Selected category: $selectedCategory", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something when nothing is selected
            }
        }
    }

    private fun updateImage() {
        binding.tvSelectedImages.text = selectedImages.size.toString()
    }

    private fun updateColors() {
        val colors = selectedColors.joinToString(" ") { Integer.toHexString(it) }
        binding.tvSelectedColors.text = colors
    }

    private fun saveProduct() {
        val name = binding.edName.text.toString().trim()
//        val category = binding.edCategory.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.offerPercentage.text.toString().trim()
        val description = binding.edDescription.text.toString().trim()
        val sizes = getSizesList(binding.edSizes.text.toString().trim())
        val imagesByteArrays = getImagesByteArrays()
        val images = mutableListOf<String>()
        val quantityProducts = binding.edQuantity.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                showLoading()
            }

            try {
                val imageUploadTasks = imagesByteArrays.map {
                    async {
                        val id = UUID.randomUUID().toString()
                        val imageStorage = productsStorage.child("products/images/$id")
                        val result = imageStorage.putBytes(it).await()
                        result.storage.downloadUrl.await().toString()
                    }
                }
                images.addAll(imageUploadTasks.awaitAll())

                val IdUser = binding.tvViewShop.text.toString()

                val product = Product(
                    UUID.randomUUID().toString(),
                    name,
                    category,
                    price.toFloat(),
                    "0",
                    IdUser,
                    quantityProducts.toInt(),
                    if (offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                    if (description.isEmpty()) null else description,
                    if (selectedColors.isEmpty()) null else selectedColors,
                    sizes,
                    images
                )

                firestore.collection("Products").add(product)
                    .addOnSuccessListener {
                        hideLoading()
                        Toast.makeText(requireContext(), "Product added successfully", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        hideLoading()
                        Log.e("Error", it.message.toString())
                        Toast.makeText(requireContext(), "Failed to add product", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hideLoading() {
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imagesByteArray = mutableListOf<ByteArray>()
        selectedImages.forEach {
            val stream = ByteArrayOutputStream()
            val imageBmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            if (imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                imagesByteArray.add(stream.toByteArray())
            }
        }
        return imagesByteArray
    }

    private fun getSizesList(sizesStr: String): List<String>? {
        if (sizesStr.isEmpty()) return null
        return sizesStr.split(",")
    }

    private fun validateInformation(): Boolean {
        val categorySelected = binding.spinnerCategory.selectedItemPosition != AdapterView.INVALID_POSITION
        return binding.edPrice.text.toString().trim().isNotEmpty() &&
                binding.edName.text.toString().trim().isNotEmpty() &&
//                binding.edCategory.text.toString().trim().isNotEmpty() &&
                categorySelected &&
                selectedImages.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
