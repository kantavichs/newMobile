package com.example.sos.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sos.R

/**
 * A simple [Fragment] subclass for displaying report related content.
 */
class ReportFragment : Fragment() {

    private var reportType: String? = null
    private var reportId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reportType = it.getString(ARG_REPORT_TYPE)
            reportId = it.getString(ARG_REPORT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    companion object {
        private const val ARG_REPORT_TYPE = "report_type"
        private const val ARG_REPORT_ID = "report_id"

        /**
         * Use this factory method to create a new instance of this fragment.
         *
         * @param reportType Type of the report.
         * @param reportId ID of the report.
         * @return A new instance of fragment ReportFragment.
         */
        @JvmStatic
        fun newInstance(reportType: String, reportId: String) =
            ReportFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REPORT_TYPE, reportType)
                    putString(ARG_REPORT_ID, reportId)
                }
            }

        /**
         * Create a new instance without parameters.
         */
        @JvmStatic
        fun newInstance() = ReportFragment()
    }
}