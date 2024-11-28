// UserAdapter.kt
package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView


class UserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])

        // Apply top margin only to the first item
        val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (position == 0) {
            layoutParams.topMargin = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.first_item_top_margin)
        } else {
            layoutParams.topMargin = 0
        }
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.userNameText)
        private val profileImage: CircleImageView = itemView.findViewById(R.id.userProfileImage)

        fun bind(user: User) {
            nameText.text = "${user.firstName} ${user.lastName}"
            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.placeholderimage)
                    .into(profileImage)
            }
            itemView.setOnClickListener { onUserClick(user) }
        }
    }
}