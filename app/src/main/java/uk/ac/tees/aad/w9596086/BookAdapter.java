package uk.ac.tees.aad.w9596086;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private ArrayList<Book> books;
    private Context mcontext;

    // creating constructor for array list and context.
    public BookAdapter(ArrayList<Book> books, Context mcontext) {
        this.books = books;
        this.mcontext = mcontext;
    }

    @NonNull
    @NotNull
    @Override
    public BookAdapter.BookViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BookAdapter.BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.name.setText(book.getTitle());
        holder.subTitle.setText(book.getSubtitle());
        holder.authors.setText(book.getAuthors());
        holder.publisher.setText(book.getPublisher());
        holder.pageCount.setText("No of Pages : " + book.getPageCount());
        holder.date.setText("Published Date : "+book.getPublishedDate());

        // below line is use to set image from URL in our image view.
        Picasso.get().load(book.getThumbnail()).into(holder.image);

        // below line is use to add on click listener for our item of recycler view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mcontext, BookDetails.class);
                i.putExtra("title", book.getTitle());
                i.putExtra("subtitle", book.getSubtitle());
                i.putExtra("authors", book.getAuthors());
                i.putExtra("publisher", book.getPublisher());
                i.putExtra("publishedDate", book.getPublishedDate());
                i.putExtra("description", book.getDescription());
                i.putExtra("pageCount", book.getPageCount());
                i.putExtra("thumbnail", book.getThumbnail());
                i.putExtra("previewLink", book.getPreviewLink());
                i.putExtra("infoLink", book.getInfoLink());
                i.putExtra("buyLink", book.getBuyLink());
                mcontext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        TextView name, subTitle, publisher, authors, pageCount, date;
        ImageView image;
        public BookViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subTitle);
            authors = itemView.findViewById(R.id.authors);
            publisher = itemView.findViewById(R.id.publisher);
            pageCount = itemView.findViewById(R.id.pageCount);
            date = itemView.findViewById(R.id.date);
            image = itemView.findViewById(R.id.image);
        }
    }
}
