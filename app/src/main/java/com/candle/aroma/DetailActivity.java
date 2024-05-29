package com.candle.aroma;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.example.aroma.R;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    TextView detailDesc, detailTitle, detailPrice;
    RelativeLayout delete_edit;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    ImageButton likeButton;
    String key = "";
    String imageUrl = "";
    DatabaseReference likesRef;
    TextView quantityTextView, quantityValueTextView;
    ImageButton increaseQuantityButton, decreaseQuantityButton;
    RatingBar ratingBar;
    EditText reviewEditText;
    Button submitReviewButton;
    DatabaseReference reviewsRef;
    LinearLayout reviewsLayout;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    int quantity = 1;
    boolean isLiked = false;
    boolean isReviewsVisible = false;
    private boolean isArrowUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailDesc = findViewById(R.id.detailDesc);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailPrice = findViewById(R.id.detailPrice);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        likeButton = findViewById(R.id.likeButton);
        delete_edit = findViewById(R.id.delete_edit);
        reviewsLayout = findViewById(R.id.reviewsLayout);

        ImageView arrowImageView = findViewById(R.id.arrow);

        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reviewsRef = database.getReference("reviews");

        if (reviewsRef == null) {
            Log.e("DetailActivity", "DatabaseReference is null");
        } else {
            fetchAndDisplayReviews();
        }


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            detailPrice.setText(bundle.getString("Price"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }

        fetchAndDisplayReviews();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            if (currentUserEmail != null && currentUserEmail.equals("sargis.arakelyan33@gmail.com")) {
                delete_edit.setVisibility(View.VISIBLE);
            } else {
                delete_edit.setVisibility(View.GONE);
            }
        }

        reviewsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReviewsVisible = !isReviewsVisible;
                reviewsRecyclerView.setVisibility(isReviewsVisible ? View.VISIBLE : View.GONE);
                if (isArrowUp) {
                    arrowImageView.setImageResource(R.drawable.up_arrow);
                } else {
                    arrowImageView.setImageResource(R.drawable.down_arrow);
                }
                isArrowUp = !isArrowUp;
            }
        });

        ratingBar = findViewById(R.id.ratingBar);
        reviewEditText = findViewById(R.id.reviewEditText);
        submitReviewButton = findViewById(R.id.submitReviewButton);

        submitReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(key).removeValue();
                        Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), CatalogActivity.class));
                        finish();
                    }
                });
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, UpdateActivity.class)
                        .putExtra("Title", detailTitle.getText().toString())
                        .putExtra("Description", detailDesc.getText().toString())
                        .putExtra("Price", detailPrice.getText().toString())
                        .putExtra("Image", imageUrl)
                        .putExtra("Key", key);
                startActivity(intent);
            }
        });

        quantityTextView = findViewById(R.id.quantityTextView);
        quantityValueTextView = findViewById(R.id.quantityValueTextView);
        increaseQuantityButton = findViewById(R.id.increaseQuantityButton);
        decreaseQuantityButton = findViewById(R.id.decreaseQuantityButton);

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                quantityValueTextView.setText(String.valueOf(quantity));
                updateQuantityInDatabase();
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    quantityValueTextView.setText(String.valueOf(quantity));
                    updateQuantityInDatabase();
                }
            }
        });


        Button addToCartButton = findViewById(R.id.addToCartButton);
        checkIfInCart(addToCartButton);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addToCartButton.getText().toString().equals("Remove from Cart")) {
                    removeFromCart(addToCartButton);
                } else {
                    addToCart(addToCartButton);
                }
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference().child("user_likes").child(userId).child(key);
                    if (isLiked) {
                        userLikesRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
                                    Toast.makeText(DetailActivity.this, "Unliked", Toast.LENGTH_SHORT).show();
                                    isLiked = false;
                                } else {
                                    Toast.makeText(DetailActivity.this, "Failed to unlike", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        userLikesRef.setValue(getProductDetails()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    likeButton.setImageResource(R.drawable.baseline_favorite_24_red);
                                    Toast.makeText(DetailActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                                    // Update isLiked flag
                                    isLiked = true;
                                } else {
                                    Toast.makeText(DetailActivity.this, "Failed to like", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void addToCart(final Button addToCartButton) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("user_cart").child(userId).child(key);

            Map<String, Object> productDetails = getProductDetails();
            productDetails.put("quantity", quantity);

            userCartRef.setValue(productDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        addToCartButton.setText("Remove from Cart");
                        Toast.makeText(DetailActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailActivity.this, "Failed to add to Cart", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void removeFromCart(final Button addToCartButton) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("user_cart").child(userId).child(key);
            userCartRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        addToCartButton.setText("Add To Cart");
                        Toast.makeText(DetailActivity.this, "Removed from Cart", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailActivity.this, "Failed to remove from Cart", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkIfInCart(final Button addToCartButton) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("user_cart").child(userId).child(key);
            userCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.child("quantity").getValue() != null) {
                        addToCartButton.setText("Remove from Cart");
                        quantity = dataSnapshot.child("quantity").getValue(Integer.class);
                        quantityValueTextView.setText(String.valueOf(quantity));
                    } else {
                        addToCartButton.setText("Add To Cart");
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    private void updateQuantityInDatabase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("user_cart").child(userId).child(key);

            userCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userCartRef.child("quantity").setValue(quantity);
                    } else {
                        Toast.makeText(DetailActivity.this, "Item is not in the cart", Toast.LENGTH_SHORT).show();
                        quantityValueTextView.setText(String.valueOf(quantity));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    private void fetchAndDisplayReviews() {
        if (TextUtils.isEmpty(key)) {
            Log.e("DetailActivity", "Product key is null or empty in fetchAndDisplayReviews");
            return;
        }

        DatabaseReference productReviewsRef = reviewsRef.child(key);
        productReviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewList.clear();
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    reviewList.add(review);
                }
                reviewAdapter.notifyDataSetChanged();
                Log.d("DetailActivity", "Number of reviews fetched: " + reviewList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DetailActivity", "Failed to load reviews", databaseError.toException());
            }
        });
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = reviewEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Please enter your review comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String userId = currentUser.getUid();
            String username = currentUser.getEmail();

            DatabaseReference userPurchasesRef = FirebaseDatabase.getInstance().getReference()
                    .child("user_purchases").child(userId);
            userPurchasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild(key)) {
                        String reviewId = reviewsRef.child(key).push().getKey();
                        Map<String, Object> reviewData = new HashMap<>();
                        reviewData.put("userEmail", username);
                        reviewData.put("rating", rating);
                        reviewData.put("comment", comment);

                        reviewsRef.child(key).child(reviewId).setValue(reviewData)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(DetailActivity.this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                                            ratingBar.setRating(0);
                                            reviewEditText.getText().clear();
                                            fetchAndDisplayReviews();
                                        } else {
                                            Toast.makeText(DetailActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(DetailActivity.this, "You need to buy the product before reviewing it", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        } else {
            // User is not authenticated
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fetchAndDisplayReviews();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference().child("user_likes").child(userId).child(key);
            userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        likeButton.setImageResource(R.drawable.baseline_favorite_24_red);
                        isLiked = true;
                    } else {
                        likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
                        isLiked = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }


    private Map<String, Object> getProductDetails() {
        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("dataDesc", detailDesc.getText().toString());
        productDetails.put("dataImage", imageUrl);
        productDetails.put("dataPrice", detailPrice.getText().toString());
        productDetails.put("dataTitle", detailTitle.getText().toString());
        return productDetails;
    }
}
