package com.example.sentinelle

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.example.sentinelle.Journey

class JourneyAdapter (
    var mContext : Context,
    var resource: Int,
    var values: ArrayList<Journey>
    ): ArrayAdapter<Journey>(mContext, resource, values){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val post = values[position]
            val itemView = LayoutInflater.from(mContext).inflate(resource, parent, false)

            val tvTitre = itemView.findViewById<TextView>(R.id.titre)
            tvTitre.text = post.titre

            val tvDesc = itemView.findViewById<TextView>(R.id.description)
            tvDesc.text = post.description

            val BtnShowPopup = itemView.findViewById<ImageView>(R.id.BtnShowPopUp)

            val tvImage = itemView.findViewById<ImageView>(R.id.avatar)
            tvImage.setImageResource(post.image)

            val tvTemps = itemView.findViewById<TextView>(R.id.duree_journey)
            tvTemps.text = post.temps


            BtnShowPopup.setOnClickListener{
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.menuInflater.inflate(R.menu.list_pop_up_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId){
                        R.id.itemShow ->{
                            Intent(mContext, detailSafeJourney_activity::class.java).also{
                                it.putExtra("titre", post.titre)
                                mContext.startActivity(it)
                            }
                        }
                        R.id.itemDelete ->{
                            values.removeAt(position)
                            notifyDataSetChanged()
                        }
                    }
                    true
                }


                popupMenu.show()
            }
            return itemView
        }
    }