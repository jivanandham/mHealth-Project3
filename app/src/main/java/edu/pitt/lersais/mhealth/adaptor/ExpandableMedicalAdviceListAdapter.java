package edu.pitt.lersais.mhealth.adaptor;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.pitt.lersais.mhealth.R;
import edu.pitt.lersais.mhealth.model.MedicalAdviceRecord;
import edu.pitt.lersais.mhealth.util.BitmapUtil;

/**
 * The group and the child has the same content
 * The difference: child item has more specific information of data, e.g, MedicalAdviceRecord
 *
 * Created by runhua and haobing on 7/24/18.
 */

public class ExpandableMedicalAdviceListAdapter extends BaseExpandableListAdapter {

    private List<MedicalAdviceRecord> mMedicalAdviceRecordList;
    private ExpandableListView expandableListView;
    private Context context;

    public ExpandableMedicalAdviceListAdapter() {}

    public ExpandableMedicalAdviceListAdapter(Context context,
                                              List<MedicalAdviceRecord> medicalAdviceRecordList,
                                              ExpandableListView view) {
        super();
        this.context = context;
        this.mMedicalAdviceRecordList = medicalAdviceRecordList;
        this.expandableListView = view;
    }

    @Override
    public int getGroupCount() {
        return mMedicalAdviceRecordList.size();
    }

    @Override
    public int getChildrenCount(int group) {
        return 1;
    }

    @Override
    public Object getGroup(int group) {
        return mMedicalAdviceRecordList.get(group);
    }

    @Override
    public Object getChild(int group, int child) {
        return mMedicalAdviceRecordList.get(group);
    }

    @Override
    public long getGroupId(int group) {
        return group;
    }

    @Override
    public long getChildId(int group, int child) {
        return child;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int group, boolean b, View view, ViewGroup viewGroup) {
        MedicalAdviceRecord medicalAdviceRecord = (MedicalAdviceRecord) getGroup(group);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_medical_case_item, null);
        }

        TextView marTextView = view.findViewById(R.id.expandable_medical_case_item);
        marTextView.setText(medicalAdviceRecord.getTitle());
        marTextView.setTypeface(null, Typeface.BOLD);

        return view;
    }

    @Override
    public View getChildView(int group, int child, boolean b, View view, ViewGroup viewGroup) {
        MedicalAdviceRecord medicalAdviceRecord = (MedicalAdviceRecord) getGroup(group);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_medical_case_subitem, null);
        }

        TextView marTitleTextView = view.findViewById(R.id.expandable_medical_case_name);
        marTitleTextView.setText(medicalAdviceRecord.getTitle());
        TextView marContentTextView = view.findViewById(R.id.expandable_medical_case_content);
        marContentTextView.setText(medicalAdviceRecord.getContent());

        ImageView marImageView = view.findViewById(R.id.expandable_medical_case_image);
        marImageView.setImageBitmap(BitmapUtil.stringToBitmap(medicalAdviceRecord.getBitmap()));

        return view;
    }

    @Override
    public boolean isChildSelectable(int group, int child) {
        return true;
    }
}
