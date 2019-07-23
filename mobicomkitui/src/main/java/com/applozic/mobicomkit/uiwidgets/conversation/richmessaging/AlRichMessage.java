package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.lists.AlRichListsAdapter;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by ashish on 28/02/18.
 */

public class AlRichMessage {
    public static final String SEND_GUEST_LIST = "sendGuestList";
    public static final String SEND_HOTEL_RATING = "sendHotelRating";
    public static final String SEND_HOTEL_DETAILS = "sendHotelDetails";
    public static final String SEND_ROOM_DETAILS_MESSAGE = "sendRoomDetailsMessage";
    public static final String SEND_BOOKING_DETAILS = "sendBookingDetails";
    public static final String MAKE_PAYMENT = "makePayment";
    public static final String TEMPLATE_ID = "templateId_";
    public static final String LINK_URL = "linkUrl";
    public static final String WEB_LINK = "link";
    public static final String QUICK_REPLY = "quickReply";
    public static final String QUICK_REPLY_OLD = "quick_reply";
    public static final String SUBMIT_BUTTON = "submit";
    public static final String KM_FAQ_ID = "KM_FAQ_ID";
    public static final String KM_SOURCE = "source";
    public static final String KM_FORM_DATA = "formData";
    public static final String KM_FORM_ACTION = "formAction";


    private Context context;
    private Message message;
    private ALRichMessageListener listener;
    private LinearLayout containerView;
    private AlCustomizationSettings alCustomizationSettings;

    public AlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        this.context = context;
        this.message = message;
        this.listener = listener;
        this.containerView = containerView;
        this.alCustomizationSettings = alCustomizationSettings;
    }

    public void createRichMessage() {
        ALRichMessageModel model = (ALRichMessageModel) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), ALRichMessageModel.class);

        LinearLayout listItemlayout = containerView.findViewById(R.id.alListMessageLayout);
        LinearLayout faqReplyLayout = containerView.findViewById(R.id.alFaqReplyLayout);
        LinearLayout faqLayout = containerView.findViewById(R.id.alFaqLayout);
        RecyclerView recyclerView = containerView.findViewById(R.id.alRichMessageContainer);
        KmCustomLayoutManager quickRepliesRecycler = containerView.findViewById(R.id.alQuickReplyRecycler);

        if (model.getTemplateId() == 7) {
            listItemlayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            faqLayout.setVisibility(View.GONE);
            faqReplyLayout.setVisibility(View.GONE);
            quickRepliesRecycler.setVisibility(View.GONE);
            setupListItemView(listItemlayout, model);
        } else if (model.getTemplateId() == 8) {
            faqLayout.setVisibility(View.VISIBLE);
            faqReplyLayout.setVisibility(View.VISIBLE);
            listItemlayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            quickRepliesRecycler.setVisibility(View.GONE);
            setupFaqItemView(faqLayout, faqReplyLayout, model);
        } else if (model.getTemplateId() == 9) {
            listItemlayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            faqLayout.setVisibility(View.GONE);
            faqReplyLayout.setVisibility(View.GONE);
            quickRepliesRecycler.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            AlImageAdapter imageAdapter = new AlImageAdapter(context, model, listener, message, alCustomizationSettings);
            recyclerView.setAdapter(imageAdapter);
        } else if (model.getTemplateId() == 3 || model.getTemplateId() == 6) {
            listItemlayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            faqLayout.setVisibility(View.GONE);
            faqReplyLayout.setVisibility(View.GONE);
            quickRepliesRecycler.setVisibility(View.VISIBLE);
            setUpGridView(quickRepliesRecycler, model);
        } else if (model.getTemplateId() > 0) {
            listItemlayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            faqLayout.setVisibility(View.GONE);
            faqReplyLayout.setVisibility(View.GONE);
            quickRepliesRecycler.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            ALRichMessageAdapter adapter = new ALRichMessageAdapter(context, model, listener, message);
            recyclerView.setAdapter(adapter);
        } else {
            containerView.setVisibility(View.GONE);
        }
    }

    private void setupFaqItemView(LinearLayout faqLayout, LinearLayout faqReplyLayout, ALRichMessageModel model) {
        if (model != null) {
            TextView headerText = faqLayout.findViewById(R.id.headerText);
            TextView titleText = faqLayout.findViewById(R.id.questionText);
            TextView descriptionText = faqLayout.findViewById(R.id.bodyText);
            TextView buttonLabel = faqReplyLayout.findViewById(R.id.buttonLabel);
            TextView actionYes = faqReplyLayout.findViewById(R.id.actionYes);
            TextView actionNo = faqReplyLayout.findViewById(R.id.actionNo);

            if (model.getPayload() != null) {
                ALRichMessageModel.ALPayloadModel payload = (ALRichMessageModel.ALPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel.class);
                if (payload != null) {
                    if (!TextUtils.isEmpty(payload.getTitle())) {
                        titleText.setVisibility(View.VISIBLE);
                        titleText.setText(getHtmlText(payload.getTitle()));
                    } else {
                        titleText.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(payload.getDescription())) {
                        descriptionText.setVisibility(View.VISIBLE);
                        descriptionText.setText(getHtmlText(payload.getDescription()));
                    } else {
                        descriptionText.setVisibility(View.GONE);
                    }

                    List<ALRichMessageModel.AlButtonModel> actionModel = payload.getButtons();
                    if (actionModel != null) {
                        faqReplyLayout.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(payload.getButtonLabel())) {
                            buttonLabel.setVisibility(View.VISIBLE);
                            buttonLabel.setText(payload.getButtonLabel());
                        } else {
                            buttonLabel.setVisibility(View.GONE);
                        }

                        if (actionModel.size() > 0 && actionModel.get(0) != null) {
                            if (!TextUtils.isEmpty(actionModel.get(0).getName())) {
                                actionYes.setVisibility(View.VISIBLE);
                                actionYes.setText(actionModel.get(0).getName());
                                setActionListener(actionYes, model, actionModel.get(0), payload);
                            } else {
                                actionYes.setVisibility(View.GONE);
                            }
                        }

                        if (actionModel.size() > 1 && actionModel.get(1) != null) {
                            if (!TextUtils.isEmpty(actionModel.get(1).getName())) {
                                actionNo.setVisibility(View.VISIBLE);
                                actionNo.setText(actionModel.get(1).getName());
                                setActionListener(actionNo, model, actionModel.get(1), payload);
                            } else {
                                actionNo.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        faqReplyLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void setActionListener(View view, final ALRichMessageModel model, final ALRichMessageModel.AlButtonModel buttonModel, final ALRichMessageModel.ALPayloadModel payloadModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(model, buttonModel), message, buttonModel, payloadModel != null ? payloadModel.getReplyMetadata() : null);
                } else {
                    if (listener != null) {
                        listener.onAction(context, getActionType(model, buttonModel), message, buttonModel, payloadModel != null ? payloadModel.getReplyMetadata() : null);
                    }
                }
            }
        });
    }

    private String getActionType(ALRichMessageModel model, ALRichMessageModel.AlButtonModel buttonModel) {
        if (buttonModel != null) {
            if (!TextUtils.isEmpty(buttonModel.getType())) {
                return buttonModel.getType();
            }
            if (buttonModel.getAction() != null && !TextUtils.isEmpty(buttonModel.getAction().getType())) {
                return buttonModel.getAction().getType();
            }
        }
        return TEMPLATE_ID + model.getTemplateId();
    }

    private void setupListItemView(LinearLayout listItemLayout, ALRichMessageModel model) {
        if (model != null) {
            if (model.getPayload() != null) {
                TextView headerText = listItemLayout.findViewById(R.id.headerText);
                ImageView headerImage = listItemLayout.findViewById(R.id.headerImage);
                ALRichMessageModel.ALPayloadModel payload = (ALRichMessageModel.ALPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel.class);
                if (payload != null) {
                    RecyclerView listRecycler = listItemLayout.findViewById(R.id.alListItemRecycler);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                    listRecycler.setLayoutManager(layoutManager);
                    AlRichListsAdapter adapter = new AlRichListsAdapter(context, message, payload.getElements(), payload.getReplyMetadata(), listener);
                    listRecycler.setAdapter(adapter);

                    if (!TextUtils.isEmpty(payload.getHeaderText())) {
                        headerText.setVisibility(View.VISIBLE);
                        headerText.setText(getHtmlText(payload.getHeaderText()));
                    } else {
                        headerText.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(payload.getHeaderImgSrc())) {
                        headerImage.setVisibility(View.VISIBLE);
                        Glide.with(context).load(payload.getHeaderImgSrc()).into(headerImage);
                    } else {
                        headerImage.setVisibility(View.GONE);
                    }

                    if (payload.getButtons() != null) {
                        final List<ALRichMessageModel.AlButtonModel> action = payload.getButtons();

                        if (action.get(0) != null) {
                            final TextView actionText1 = listItemLayout.findViewById(R.id.actionButton1);
                            actionText1.setVisibility(View.VISIBLE);
                            actionText1.setText(action.get(0).getName());
                            setActionListener(actionText1, model, action.get(0), payload);
                        }

                        if (action.size() > 1 && action.get(1) != null) {
                            final TextView actionText2 = listItemLayout.findViewById(R.id.actionButton2);
                            View actionDivider2 = listItemLayout.findViewById(R.id.actionDivider2);
                            actionDivider2.setVisibility(View.VISIBLE);
                            actionText2.setVisibility(View.VISIBLE);
                            actionText2.setText(action.get(1).getName());
                            setActionListener(actionText2, model, action.get(1), payload);
                        }

                        if (action.size() > 2 && action.get(2) != null) {
                            final TextView actionText3 = listItemLayout.findViewById(R.id.actionButton3);
                            View actionDivider3 = listItemLayout.findViewById(R.id.actionDivider3);
                            actionDivider3.setVisibility(View.VISIBLE);
                            actionText3.setVisibility(View.VISIBLE);
                            actionText3.setText(action.get(2).getName());
                            setActionListener(actionText3, model, action.get(2), payload);
                        }
                    }
                }
            }
        }
    }

    private void setUpGridView(KmCustomLayoutManager flowLayout, final ALRichMessageModel model) {

        final List<ALRichMessageModel.ALPayloadModel> payloadList = Arrays.asList((ALRichMessageModel.ALPayloadModel[])
                GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel[].class));

        if (model.getTemplateId() == 3 || model.getTemplateId() == 6) {
            flowLayout.removeAllViews();
            for (final ALRichMessageModel.ALPayloadModel payloadModel : payloadList) {
                View view = LayoutInflater.from(context).inflate(R.layout.al_rich_message_single_text_item, null);
                TextView itemTextView = view.findViewById(R.id.singleTextItem);

                if (model.getTemplateId() == 3) {
                    if (!TextUtils.isEmpty(payloadModel.getName())) {
                        itemTextView.setText(payloadModel.getName().trim());
                    } else {
                        itemTextView.setText("");
                    }
                } else {
                    if (payloadModel.getTitle() != null) {
                        itemTextView.setText(payloadModel.getTitle().trim());
                    } else {
                        itemTextView.setText("");
                    }
                }

                itemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.getTemplateId() == 6) {
                            if (context.getApplicationContext() instanceof ALRichMessageListener) {
                                ((ALRichMessageListener) context.getApplicationContext()).onAction(context, TEMPLATE_ID + model.getTemplateId(), message, payloadModel, payloadModel.getReplyMetadata());
                            } else {
                                listener.onAction(context, AlRichMessage.SEND_HOTEL_RATING, message, payloadModel.getMessage().trim(), payloadModel.getReplyMetadata());
                            }
                        } else {
                            if (context.getApplicationContext() instanceof ALRichMessageListener) {
                                ((ALRichMessageListener) context.getApplicationContext()).onAction(context, TEMPLATE_ID + model.getTemplateId(), message, payloadModel, payloadModel.getReplyMetadata());
                            } else {
                                if (!TextUtils.isEmpty(model.getFormData()) && !TextUtils.isEmpty(model.getFormAction())) {
                                    listener.onAction(context, AlRichMessage.MAKE_PAYMENT, message, model, payloadModel != null ? payloadModel.getReplyMetadata() : null);
                                } else {
                                    listener.onAction(context, AlRichMessage.WEB_LINK, message, payloadModel, payloadModel.getReplyMetadata());
                                }
                            }
                        }
                    }
                });

                flowLayout.addView(view);
            }
        }
    }

    public static Spanned getHtmlText(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(message);
        }
    }
}