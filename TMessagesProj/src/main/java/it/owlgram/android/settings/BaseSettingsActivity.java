package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;

import java.util.List;
import java.util.Locale;

public abstract class BaseSettingsActivity extends BaseFragment {
    protected static final Object PARTIAL = new Object();
    protected int rowCount;
    protected BaseListAdapter listAdapter;
    protected Context context;
    protected RecyclerListView listView;
    protected ActionBarMenuItem menuItem;
    protected UndoView restartTooltip;
    protected EmptyTextProgressView emptyView;

    // VIEW TYPES
    protected static final int TYPE_SHADOW = 0;
    protected static final int TYPE_HEADER = 1;
    protected static final int TYPE_HEADER_NO_SHADOW = 2;
    protected static final int TYPE_CHECKBOX = 3;
    protected static final int TYPE_TEXT_RADIO = 4;
    protected static final int TYPE_SWITCH = 5;
    protected static final int TYPE_TEXT_CELL = 6;
    protected static final int TYPE_TEXT_HINT = 7;
    protected static final int TYPE_TEXT_HINT_WITH_PADDING = 8;
    protected static final int TYPE_SETTINGS = 9;
    protected static final int TYPE_DETAILED_SETTINGS = 10;
    protected static final int TYPE_ADD_EXCEPTION = 11;
    protected static final int TYPE_RADIO = 12;

    protected abstract String getActionBarTitle();

    protected ActionBarMenuItem createMenuItem() {
        return null;
    }

    protected boolean haveEmptyView() {
        return false;
    }

    @Override
    public View createView(Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(getActionBarTitle());
        actionBar.setAllowOverlayTitle(true);
        menuItem = createMenuItem();
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else {
                    onMenuItemClick(id);
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter = createAdapter());
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(this::onItemClick);

        if (haveEmptyView()) {
            emptyView = new EmptyTextProgressView(context);
            emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
            emptyView.showTextView();
            emptyView.setShowAtCenter(true);
            frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            listView.setEmptyView(emptyView);
            listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }
            });
        }

        restartTooltip = new UndoView(context);
        restartTooltip.setInfoText(LocaleController.formatString("RestartAppToApplyChanges", R.string.RestartAppToApplyChanges));
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));
        return fragmentView;
    }

    protected void onItemClick(View view, int position, float x, float y) {

    }

    protected void onMenuItemClick(int id) {

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    protected void updateRowsId() {
        rowCount = 0;
    }

    protected abstract BaseListAdapter createAdapter();

    protected abstract class BaseListAdapter extends RecyclerListView.SelectionAdapter {
        @Override
        public int getItemCount() {
            return rowCount;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            onBindViewHolder(holder, position, PARTIAL.equals(payload));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            onBindViewHolder(holder, position, false);
        }

        protected abstract void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial);

        @NonNull
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            boolean canSetBackground = true;
            switch (viewType) {
                case TYPE_SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case TYPE_HEADER:
                case TYPE_HEADER_NO_SHADOW:
                    view = new HeaderCell(context);
                    break;
                case TYPE_CHECKBOX:
                    view = new TextCheckbox2Cell(context);
                    break;
                case TYPE_TEXT_RADIO:
                    view = new TextRadioCell(context);
                    break;
                case TYPE_SWITCH:
                    view = new TextCheckCell(context);
                    break;
                case TYPE_TEXT_CELL:
                    view = new TextCell(context);
                    break;
                case TYPE_TEXT_HINT:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case TYPE_TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(context);
                    textInfoPrivacyCell.setBottomPadding(16);
                    view = textInfoPrivacyCell;
                    break;
                case TYPE_SETTINGS:
                    view = new TextSettingsCell(context);
                    break;
                case TYPE_DETAILED_SETTINGS:
                    view = new TextDetailSettingsCell(context);
                    break;
                case TYPE_ADD_EXCEPTION:
                    view = new ManageChatTextCell(context);
                    break;
                case TYPE_RADIO:
                    view = new RadioCell(context);
                    break;
                default:
                    view = onCreateViewHolder(viewType);
                    canSetBackground = false;
                    if (view == null) throw new IllegalArgumentException("Unknown viewType: " + viewType);
            }
            switch (viewType) {
                case TYPE_HEADER_NO_SHADOW:
                case TYPE_SHADOW:
                case TYPE_TEXT_HINT:
                case TYPE_TEXT_HINT_WITH_PADDING:
                    break;
                default:
                    if (canSetBackground) view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        protected View onCreateViewHolder(int viewType) {
            return null;
        }
    }

    protected static String getLanguage(String language) {
        Locale locale = Locale.forLanguageTag(language);
        if (!TextUtils.isEmpty(locale.getScript())) {
            return HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale)));
        }
    }
}