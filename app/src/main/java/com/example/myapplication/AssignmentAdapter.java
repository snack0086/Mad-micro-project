package com.example.myapplication;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    Context context;
    ArrayList<Assignment> list;
    String userRole;

    public AssignmentAdapter(Context context, ArrayList<Assignment> list, String userRole) {
        this.context = context;
        this.list = list;
        this.userRole = userRole;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDesc, tvCount;
        Button btnSubmit, btnComment;
        EditText etComment;
        RecyclerView rvComments;

        // Store listener refs so we can detach on recycle
        ValueEventListener submissionListener;
        ValueEventListener commentListener;
        DatabaseReference submissionRef;
        DatabaseReference commentRef;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDescription);
            tvCount = itemView.findViewById(R.id.tvSubmissionCount);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
            btnComment = itemView.findViewById(R.id.btnComment);
            etComment = itemView.findViewById(R.id.etComment);
            rvComments = itemView.findViewById(R.id.rvComments);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Assignment a = list.get(position);
        holder.tvTitle.setText(a.title);
        holder.tvDesc.setText(a.description);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Assignments")
                .child(a.id);

        // 🔹 Submission visibility
        if ("student".equals(userRole)) {
            holder.tvCount.setVisibility(View.GONE);
        } else {
            holder.btnSubmit.setVisibility(View.GONE);
        }

        // 🔹 Submission count (Teacher) — detach old listener first
        if (holder.submissionRef != null && holder.submissionListener != null) {
            holder.submissionRef.removeEventListener(holder.submissionListener);
        }
        holder.submissionRef = ref.child("submissions");
        holder.submissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                holder.tvCount.setText("Submissions: " + count);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        holder.submissionRef.addValueEventListener(holder.submissionListener);

        // 🔹 Submit (Student) — null-check currentUser
        holder.btnSubmit.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseUser user =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            ref.child("submissions").child(user.getUid()).setValue(true);
            Toast.makeText(context, "Submitted", Toast.LENGTH_SHORT).show();
        });

        // 🔹 Load Comments — detach old listener first
        ArrayList<Comment> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(commentList);
        holder.rvComments.setLayoutManager(new LinearLayoutManager(context));
        holder.rvComments.setAdapter(commentAdapter);

        if (holder.commentRef != null && holder.commentListener != null) {
            holder.commentRef.removeEventListener(holder.commentListener);
        }
        holder.commentRef = ref.child("comments");
        holder.commentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comment c = ds.getValue(Comment.class);
                    commentList.add(c);
                }
                commentAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        holder.commentRef.addValueEventListener(holder.commentListener);

        // 🔹 Post Comment — null-check currentUser
        holder.btnComment.setOnClickListener(v -> {
            String text = holder.etComment.getText().toString().trim();
            if (text.isEmpty()) return;

            com.google.firebase.auth.FirebaseUser user =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String commentId = ref.child("comments").push().getKey();

            HashMap<String,Object> map = new HashMap<>();
            map.put("userUid", user.getUid());
            map.put("userRole", userRole);
            map.put("text", text);
            map.put("timestamp", System.currentTimeMillis());

            ref.child("comments").child(commentId).setValue(map);
            holder.etComment.setText("");
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Detach listeners when view is recycled to prevent listener leaks
        if (holder.submissionRef != null && holder.submissionListener != null) {
            holder.submissionRef.removeEventListener(holder.submissionListener);
        }
        if (holder.commentRef != null && holder.commentListener != null) {
            holder.commentRef.removeEventListener(holder.commentListener);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
