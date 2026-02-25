package com.example.tripset

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class EditTripFragment : Fragment(R.layout.fragment_edit_trip) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("General Tasks"))
        tabLayout.addTab(tabLayout.newTab().setText("Packing List"))
        tabLayout.addTab(tabLayout.newTab().setText("Documents"))

        // ברירת מחדל – Tasks
        childFragmentManager.beginTransaction()
            .replace(R.id.tabContainer, TabTasksFragment())
            .commit()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val fragment = when (tab?.position) {
                    0 -> TabTasksFragment()
                    1 -> TabPackingFragment()
                    2 -> TabDocumentsFragment()
                    else -> TabTasksFragment()
                }

                childFragmentManager.beginTransaction()
                    .replace(R.id.tabContainer, fragment)
                    .commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // (אופציונלי) אם יש לך כפתור חזור ב-XML עם id btnBack:
        view.findViewById<ImageButton?>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}