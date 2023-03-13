package uk.ac.tees.aad.w9596086;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shockwave.pdfium.PdfiumCore;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyEBooks extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
//    private RecyclerView mRecyclerView;
//    private PdfFileAdapter mAdapter;
//    private List<PdfFile> mPdfFilesList = new ArrayList<>();
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ebooks);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = firestore.collection("pdf_files").document(userId).collection("myPDF");

        FirestoreRecyclerOptions<PdfFile> options=new FirestoreRecyclerOptions.Builder<PdfFile>()
                .setQuery(query,PdfFile.class)
                .build();

        FirestoreRecyclerAdapter<PdfFile, PdfFileViewHolder> adapter =
                new FirestoreRecyclerAdapter<PdfFile, PdfFileViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PdfFileViewHolder holder, int position, @NonNull PdfFile model) {
                        // Bind the PdfFile object to the view holder
                        holder.bind(model);
                    }

                    @NonNull
                    @Override
                    public PdfFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        // Create a new view holder
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.pdf_file_item, parent, false);
                        return new PdfFileViewHolder(view);
                    }
                };
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //        mRecyclerView = findViewById(R.id.recycler_view);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mAdapter = new PdfFileAdapter(mPdfFilesList);
//        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
//        storageRef=FirebaseStorage.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(MyEBooks.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickPdfFile();
                }
            }
        });

//        loadPdfFiles();
    }

    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Upload File")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MyEBooks.this,new String[]  {Manifest.permission.READ_EXTERNAL_STORAGE},PICK_PDF_REQUEST);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},PICK_PDF_REQUEST);
        }
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF file"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pdfUri = data.getData();
            try{
                uploadPdfFile(pdfUri);
            }catch(Exception e){
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }

        }
    }

    private void uploadPdfFile(Uri pdfUri) {
        String fileName = getFileName(pdfUri);
//        StorageReference thumbnailRef = mStorage.getReference().child("pdf_thumbnails/" + fileName);
        StorageReference pdfStorageRef = mStorage.getReference().child("pdf_files/"+fileName);
        UploadTask uploadTask = pdfStorageRef.putFile(pdfUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pdfStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        generatePdfThumbnail(pdfUri, fileName);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to upload PDF file", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to upload PDF file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdfThumbnail(Uri pdfUri, String fileName) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(this);
        try {
            ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(pdfUri, "r");
            com.shockwave.pdfium.PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbnailBytes = baos.toByteArray();
            baos.close();

            StorageReference thumbnailRef = mStorage.getReference().child("pdf_thumbnails/" + fileName);
            UploadTask uploadThumbnailTask = thumbnailRef.putBytes(thumbnailBytes);

            uploadThumbnailTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return thumbnailRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri thumbnailDownloadUri = task.getResult();
                        savePdfFileToFirestore(fileName, pdfUri.toString(), thumbnailDownloadUri.toString());
                    } else {
                        Toast.makeText(MyEBooks.this, "Failed to upload PDF thumbnail", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePdfFileToFirestore(String fileName, String fileUrl, String thumbnailUrl) {
        PdfFile pdfFile = new PdfFile(fileName, fileUrl, thumbnailUrl);
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirestore.collection("pdf_files/"+ uId +"/myPDF")
                .add(pdfFile)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
//                        pdfFile.setId(documentReference.getId());
//                        mPdfFilesList.add(pdfFile);
                        Toast.makeText(getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
//                        mAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private void savePdfFileToFirestore(String fileName, String fileUrl, String thumbnailUrl) {
//        PdfFile pdfFile = new PdfFile(fileName, fileUrl, thumbnailUrl);
//        mFirestore.collection("pdf_files")
//                .add(pdfFile)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        pdfFile.setId(documentReference.getId());
//                        mPdfFilesList.add(pdfFile);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MyEBooks.this, "Failed to save PDF file to Firestore", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//    private void loadPdfFiles() {
//        mFirestore.collection("pdf_files")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
//                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
//                            PdfFile pdfFile = documentSnapshot.toObject(PdfFile.class);
//                            pdfFile.setId(documentSnapshot.getId());
//                            mPdfFilesList.add(pdfFile);
//                        }
//                        mAdapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MyEBooks.this, "Failed to load PDF files from Firestore", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    public String getFileName(Uri uri) {
//        String fileName = null;
//
//        if (uri.getScheme().equals("content")) {
//            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
//                    int column_index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                    if(column_index != -1) {
//                        fileName = cursor.getString(column_index);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        if(fileName == null){
//            fileName = uri.getLastPathSegment();
//        }
//        return fileName;
//
//    }
    public String getFileName(Uri uri) {
        String fileName = "Untitled";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(column_index != -1) {
                        fileName = cursor.getString(column_index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(fileName == null){
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
    public class PdfFileViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private ImageView thumbnailImageView;
        public PdfFileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
        }

        public void bind(PdfFile pdfFile) {
            nameTextView.setText(pdfFile.getName());
            Glide.with(thumbnailImageView.getContext())
                    .load(pdfFile.getThumbnailUrl())
                    .into(thumbnailImageView);
        }
    }

}