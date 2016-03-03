package com.miguelcatalan.materialsearchview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Suggestions Adapter.
 *
 * @author Miguel Catalan Bañuls
 */
public class SearchAdapter extends BaseAdapter implements Filterable {

    private enum SearchTypes {
        HISTORY_TYPE,
        SUGGESTION_TYPE
    }

    private HashMap<String, SearchTypes> dataMap;
    private ArrayList<String> data;
    private String[] suggestions;
    private Drawable suggestionIcon;
    private String[] history;
    private Drawable historyIcon;
    private LayoutInflater inflater;
    private boolean ellipsize;

    public SearchAdapter(Context context, String[] suggestions, String[] history) {
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
        this.suggestions = suggestions;
        this.history = history;
        this.dataMap = new HashMap<>();
    }

    public SearchAdapter(Context context, String[] suggestions, String[] history,
                         Drawable suggestionIcon, Drawable historyIcon, boolean ellipsize) {
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
        this.suggestions = suggestions;
        this.suggestionIcon = suggestionIcon;
        this.history = history;
        this.historyIcon = historyIcon;
        this.ellipsize = ellipsize;
        this.dataMap = new HashMap<>();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                // Retrieve the autocomplete results.
                List<String> searchData = new ArrayList<>();
                dataMap.clear();
                if (!TextUtils.isEmpty(constraint)) {
                    for (String string : history) {
                        if (string.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            dataMap.put(string, SearchTypes.HISTORY_TYPE);
                            searchData.add(string);
                        }
                    }
                    for (String string : suggestions) {
                        if (string.toLowerCase().startsWith(constraint.toString().toLowerCase())
                                && !dataMap.containsKey(string)) {
                            dataMap.put(string, SearchTypes.SUGGESTION_TYPE);
                            searchData.add(string);
                        }
                    }
                } else {
                    for (String string : history) {
                        searchData.add(string);
                        dataMap.put(string, SearchTypes.HISTORY_TYPE);
                    }
                }
                // Assign the data to the FilterResults
                filterResults.values = searchData;
                filterResults.count = searchData.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null && results.values instanceof ArrayList) {
                    data = (ArrayList<String>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
        return filter;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return SearchTypes.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return dataMap.get(data.get(position)).ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SuggestionsViewHolder viewHolder;
        SearchTypes type = getTypeFromOrdinal(getItemViewType(position));

        if (convertView == null) {
            switch (type) {
                case SUGGESTION_TYPE:
                    convertView = inflater.inflate(R.layout.suggest_item, parent, false);
                    break;
                case HISTORY_TYPE:
                    convertView = inflater.inflate(R.layout.history_item, parent, false);
                    break;
                default:
                    throw new IllegalStateException("Should not be here");
            }
            viewHolder = new SuggestionsViewHolder(convertView, type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SuggestionsViewHolder) convertView.getTag();
        }

        String currentListData = (String) getItem(position);
        viewHolder.textView.setText(currentListData);
        if (ellipsize) {
            viewHolder.textView.setSingleLine();
            viewHolder.textView.setEllipsize(TextUtils.TruncateAt.END);
        }

        return convertView;
    }

    private class SuggestionsViewHolder {

        TextView textView;
        ImageView imageView;

        public SuggestionsViewHolder(View convertView, SearchTypes type) {
            switch (type) {
                case HISTORY_TYPE:
                    textView = (TextView) convertView.findViewById(R.id.history_text);
                    if (historyIcon != null) {
                        imageView = (ImageView) convertView.findViewById(R.id.history_icon);
                        imageView.setImageDrawable(historyIcon);
                    }
                    break;
                case SUGGESTION_TYPE:
                    textView = (TextView) convertView.findViewById(R.id.suggestion_text);
                    if (suggestionIcon != null) {
                        imageView = (ImageView) convertView.findViewById(R.id.suggestion_icon);
                        imageView.setImageDrawable(suggestionIcon);
                    }
                    break;
                default:
                    throw new IllegalStateException("Should not be here");
            }
        }
    }

    private static SearchTypes getTypeFromOrdinal(int ordinal) {
        return SearchTypes.values()[ordinal];
    }
}