//package uk.ac.tees.aad.w9596086;
//
//import android.support.annotation.NonNull;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//
//import java.util.List;
//
//public class PdfFileAdapter extends RecyclerView.Adapter<PdfFileAdapter.PdfFileViewHolder> {
//
//    private List<PdfFile> mPdfFilesList;
//    private OnItemClickListener mListener;
//
//    public interface OnItemClickListener {
//        void onItemClick(int position);
//    }
//
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        mListener = listener;
//    }
//
//    public static class PdfFileViewHolder extends RecyclerView.ViewHolder {
//        public TextView mTextViewName;
//        public ImageView mImageViewThumbnail;
//
//        public PdfFileViewHolder(View itemView, final OnItemClickListener listener) {
//            super(itemView);
//            mTextViewName = itemView.findViewById(R.id.text_view_pdf_file_name);
//            mImageViewThumbnail = itemView.findViewById(R.id.image_view_pdf_file_thumbnail);
//
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (listener != null) {
//                        int position = getAdapterPosition();
//                        if (position != RecyclerView.NO_POSITION) {
//                            listener.onItemClick(position);
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    public PdfFileAdapter(List<PdfFile> pdfFilesList) {
//        mPdfFilesList = pdfFilesList;
//    }
//
//    @NonNull
//    @Override
//    public PdfFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_file_item, parent, false);
//        PdfFileViewHolder viewHolder = new PdfFileViewHolder(v, mListener);
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull PdfFileViewHolder holder, int position) {
//        PdfFile currentItem = mPdfFilesList.get(position);
//        holder.mTextViewName.setText(currentItem.getName());
//
//        // Load the thumbnail image using the Glide library
//        Glide.with(holder.itemView.getContext())
//                .load(currentItem.getThumbnailUrl())
//                .into(holder.mImageViewThumbnail);
//    }
//
//    @Override
//    public int getItemCount() {
//        return mPdfFilesList.size();
//    }
//}
