package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by ashish on 28/02/18.
 */

public class ALRichMessageAdapter extends RecyclerView.Adapter {

    private Context context;
    private ALRichMessageModel model;
    private List<AlHotelBookingModel> hotelList;
    private List<AlHotelBookingModel> roomList;
    private List<ALGuestCountModel> guestList;
    private ALRichMessageListener listener;
    private List<ALRichMessageModel.ALPayloadModel> payloadList;
    private Message message;
    List<String> titleList;

    public ALRichMessageAdapter(Context context, ALRichMessageModel model, ALRichMessageListener listener, Message message) {
        this.model = model;
        this.context = context;
        this.model = model;
        this.listener = listener;
        this.message = message;

        if (model.getHotelList() != null) {
            this.hotelList = Arrays.asList((AlHotelBookingModel[])
                    GsonUtils.getObjectFromJson(model.getHotelList(), AlHotelBookingModel[].class));
        }

        if (model.getPayload() != null) {
            this.payloadList = Arrays.asList((ALRichMessageModel.ALPayloadModel[])
                    GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel[].class));
        }

        if (model.getTemplateId() == 1) {
            ALGuestCountModel guestCount = new ALGuestCountModel();
            guestList = new ArrayList<>();
            guestList.add(guestCount);
        }

        if (model.getHotelRoomDetail() != null) {
            AlHotelBookingModel.RoomDetailModel roomDetailModel = (AlHotelBookingModel.RoomDetailModel) GsonUtils.getObjectFromJson(model.getHotelRoomDetail(), AlHotelBookingModel.RoomDetailModel.class);
            this.roomList = roomDetailModel.getHotelRoomsDetails();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (model != null && model.getHotelRoomDetail() != null) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.al_hotel_details_layout, parent, false);
            return new HotelDetailHolder(itemView);
        } else if (model != null && model.getTemplateId() == 5) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.al_booking_details_layout, parent, false);
            return new BookingDetailsHolder(itemView);
        } else if (model != null && model.getTemplateId() == 1) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.al_guest_details_layout, parent, false);
            return new GuestCountHolder(itemView);
        } else if (hotelList != null) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.al_rich_message_item, parent, false);
            return new MyViewHolder(itemView);
        } else if (model != null && model.getTemplateId() == 2) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.al_rich_message_item, parent, false);
            return new MyViewHolder(itemView);
        }

        View itemView = LayoutInflater.from(context).inflate(R.layout.al_rich_message_single_text_item, parent, false);
        return new SingleTextViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (model.getHotelRoomDetail() != null) {
            HotelDetailHolder detailHolder = (HotelDetailHolder) holder;
            bindRoomDetailView(detailHolder, position);
        } else if (model.getTemplateId() == 5) {
            BookingDetailsHolder bookingDetailsHolder = (BookingDetailsHolder) holder;
            bindBookingDetailHolder(bookingDetailsHolder, position);
        } else if (hotelList != null || (model != null && model.getTemplateId() == 2)) {
            MyViewHolder viewHolder = (MyViewHolder) holder;
            bindHotelView(viewHolder, position);
        } else if (model != null && model.getTemplateId() == 1) {
            GuestCountHolder guestCountHolder = (GuestCountHolder) holder;
            bindGuestInfoView(guestCountHolder, position);
        } else if (model != null && (model.getTemplateId() == 6 || model.getTemplateId() == 3)) {
            SingleTextViewHolder singleTextViewHolder = (SingleTextViewHolder) holder;
            bindSingleTextItem(singleTextViewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (hotelList != null) {
            return hotelList.size();
        } else if (model.getTemplateId() == 1) {
            return guestList.size();
        } else if (model.getTemplateId() == 6 || model.getTemplateId() == 3) {
            return payloadList.size();
        } else if (roomList != null) {
            return roomList.size();
        } else if (model.getTemplateId() == 5) {
            return 1;
        } else if (model.getTemplateId() == 2) {
            return payloadList.size();
        }

        return 0;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout roomRootLayout;
        TextView productNameSingleLine;
        TextView productPrice;
        ImageView productImage;
        TextView productRating;
        TextView productLocation;
        TextView productDescription;
        RelativeLayout productNameSplitLayout;
        TextView productName;
        View productImageOverlay;
        TextView bookAction1, bookAction2, bookAction3;
        View viewAction1, viewAction2, viewAction3;

        public MyViewHolder(View itemView) {
            super(itemView);

            roomRootLayout = (LinearLayout) itemView.findViewById(R.id.roomRootLayout);
            productNameSingleLine = (TextView) itemView.findViewById(R.id.productNameSingleLine);
            productImage = (ImageView) itemView.findViewById(R.id.productImage);
            productRating = (TextView) itemView.findViewById(R.id.productRating);
            productLocation = (TextView) itemView.findViewById(R.id.productLocation);
            productPrice = (TextView) itemView.findViewById(R.id.productPrice);
            productDescription = (TextView) itemView.findViewById(R.id.productDescription);
            productName = itemView.findViewById(R.id.productName);
            productImageOverlay = itemView.findViewById(R.id.productImageOverlay);
            productNameSplitLayout = itemView.findViewById(R.id.productNameSplitLayout);
            productRating = itemView.findViewById(R.id.productRating);
            bookAction1 = itemView.findViewById(R.id.bookingAction1);
            bookAction2 = itemView.findViewById(R.id.bookingAction2);
            bookAction3 = itemView.findViewById(R.id.bookingAction3);
            viewAction1 = itemView.findViewById(R.id.viewAction1);
            viewAction2 = itemView.findViewById(R.id.viewAction2);
            viewAction3 = itemView.findViewById(R.id.viewAction3);
        }
    }

    private class HotelDetailHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView roomTypeTv;
        TextView noOfGuestTv;
        TextView priceTv;
        TextView totalPriceHeaderTv;
        TextView totalPriceTv;
        TextView bookAction;

        public HotelDetailHolder(View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.productImage);
            roomTypeTv = itemView.findViewById(R.id.roomTypeTv);
            noOfGuestTv = itemView.findViewById(R.id.noOfGuestsTv);
            priceTv = itemView.findViewById(R.id.hotelPriceTv);
            totalPriceHeaderTv = itemView.findViewById(R.id.totalPriceDistTv);
            totalPriceTv = itemView.findViewById(R.id.totalPriceTv);
            bookAction = itemView.findViewById(R.id.bookingAction);
        }
    }

    private class BookingDetailsHolder extends RecyclerView.ViewHolder {

        Spinner titleSpinner;
        EditText firstNameEt;
        EditText lastNameEt;
        EditText emailIdEt;
        EditText contactNumberEt;
        TextView submitAction;

        public BookingDetailsHolder(View itemView) {
            super(itemView);

            titleSpinner = itemView.findViewById(R.id.titleSpinner);
            firstNameEt = itemView.findViewById(R.id.firstNameEt);
            lastNameEt = itemView.findViewById(R.id.lastNameEt);
            emailIdEt = itemView.findViewById(R.id.emailIdEt);
            contactNumberEt = itemView.findViewById(R.id.contactNumberEt);
            submitAction = itemView.findViewById(R.id.submitDetails);

            titleList = new ArrayList<>();
            titleList.add("Title *");
            titleList.add("Mr.");
            titleList.add("Ms.");
            titleList.add("Mrs");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, titleList);
            titleSpinner.setAdapter(adapter);
        }
    }

    private class GuestCountHolder extends RecyclerView.ViewHolder {

        TextView adultCountTv, childCountTv;
        Button adultCountDecBt, childCountDecBt, adultCountIncrementBt, childCountIncrementBt;
        TextView roomDetailTv;
        TextView addRoomTv, removeRoomTv, doneTv;
        LinearLayout selectionActionLayout;
        LinearLayout selectionRootLayout;

        public GuestCountHolder(View itemView) {
            super(itemView);

            adultCountTv = itemView.findViewById(R.id.adultCountTv);
            childCountTv = itemView.findViewById(R.id.childCountTv);
            adultCountIncrementBt = itemView.findViewById(R.id.adultCountIncrementBt);
            childCountIncrementBt = itemView.findViewById(R.id.childCountIncrementBt);
            adultCountDecBt = itemView.findViewById(R.id.adultCountDecBt);
            childCountDecBt = itemView.findViewById(R.id.childCountDecBt);
            roomDetailTv = itemView.findViewById(R.id.alRoomDetailsTv);
            addRoomTv = itemView.findViewById(R.id.addRoomTv);
            removeRoomTv = itemView.findViewById(R.id.removeRoomTv);
            doneTv = itemView.findViewById(R.id.doneButtonTv);
            selectionActionLayout = itemView.findViewById(R.id.actionLayout);
            selectionRootLayout = itemView.findViewById(R.id.rootSelectionLayout);
        }
    }

    private class SingleTextViewHolder extends RecyclerView.ViewHolder {

        TextView singleTextItem;
        LinearLayout rootLayout;

        public SingleTextViewHolder(View itemView) {
            super(itemView);

            singleTextItem = itemView.findViewById(R.id.singleTextItem);
            rootLayout = itemView.findViewById(R.id.rootLayout);

            singleTextItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (model != null && model.getTemplateId() == 6) {
                        if (context.getApplicationContext() instanceof ALRichMessageListener) {
                            ((ALRichMessageListener) context.getApplicationContext()).onAction(context, "Click", message, payloadList.get(getLayoutPosition()).getMessage().trim());
                        }
                        listener.onAction(context, "sendHotelRating", null, payloadList.get(getLayoutPosition()).getMessage().trim());
                    } else {
                        if (context.getApplicationContext() instanceof ALRichMessageListener) {
                            ((ALRichMessageListener) context.getApplicationContext()).onAction(context, "Click", message, model);
                        }
                        listener.onAction(context, "makePayment", null, model);
                    }
                }
            });
        }
    }

    private void setGuestInfoClickListeners(final GuestCountHolder holder, final int position) {
        holder.adultCountDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.adultCountTv.getText().toString());
                ALGuestCountModel guestModel = guestList.get(position);
                if (count > 1) {
                    holder.adultCountTv.setText(String.valueOf(count - 1));
                    guestModel.setNoOfAdults(String.valueOf(count - 1));
                }
            }
        });

        holder.adultCountIncrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.adultCountTv.getText().toString());
                ALGuestCountModel guestModel = guestList.get(position);
                if (count < 5) {
                    holder.adultCountTv.setText(String.valueOf(count + 1));
                    guestModel.setNoOfAdults(String.valueOf(count + 1));
                }
            }
        });

        holder.childCountDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.childCountTv.getText().toString());
                ALGuestCountModel guestModel = guestList.get(position);
                if (count > 0) {
                    holder.childCountTv.setText(String.valueOf(count - 1));
                    guestModel.getChildAge().remove(guestModel.getChildAge().size() - 1);
                    guestModel.setNoOfChild(String.valueOf(count - 1));
                }
            }
        });

        holder.childCountIncrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.childCountTv.getText().toString());
                ALGuestCountModel guestModel = guestList.get(position);
                if (count < 2) {
                    holder.childCountTv.setText(String.valueOf(count + 1));
                    guestModel.getChildAge().add(10);
                    guestModel.setNoOfChild(String.valueOf(count + 1));
                }
            }
        });

        holder.addRoomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ALGuestCountModel model = new ALGuestCountModel();
                guestList.add(model);
                notifyDataSetChanged();
            }
        });

        holder.removeRoomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guestList.remove(position);
                notifyDataSetChanged();
            }
        });

        holder.doneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAction(context, "sendGuestList", null, guestList);
            }
        });
    }

    private void bindGuestInfoView(final GuestCountHolder holder, final int position) {

        final ALGuestCountModel guestModel = guestList.get(position);

        if (guestModel != null) {
            holder.roomDetailTv.setText("ROOM " + String.valueOf(position + 1));
            holder.adultCountTv.setText(guestModel.getNoOfAdults());
            holder.childCountTv.setText(guestModel.getNoOfChild());

            if (position == guestList.size() - 1) {
                holder.addRoomTv.setVisibility(View.VISIBLE);
                holder.doneTv.setVisibility(View.VISIBLE);
            } else {
                holder.addRoomTv.setVisibility(GONE);
                holder.doneTv.setVisibility(GONE);
            }

            if (guestList.size() > 1 && position == 0) {
                holder.selectionActionLayout.setVisibility(GONE);
            } else {
                holder.selectionActionLayout.setVisibility(View.VISIBLE);
            }

            if (position == 0) {
                holder.removeRoomTv.setVisibility(GONE);
            } else {
                holder.removeRoomTv.setVisibility(View.VISIBLE);
            }
            setGuestInfoClickListeners(holder, position);
        }
    }

    //templateId = 2
    private void bindHotelView(MyViewHolder viewHolder, int position) {

        if (hotelList != null) {
            final AlHotelBookingModel hotel = hotelList.get(position);

            if (!TextUtils.isEmpty(hotel.getHotelName())) {
                viewHolder.productNameSingleLine.setText(hotel.getHotelName() + " (" + hotel.getStarRating() + "/5)");
            } else {
                viewHolder.productNameSingleLine.setText("Name Unavailable (" + hotel.getStarRating() + "/5)");
            }

            viewHolder.productPrice.setText(context.getString(R.string.rupee_symbol) + " " + hotel.getPrice().getRoomPrice());

            if (!TextUtils.isEmpty(hotel.getHotelPicture())) {
                Glide.with(context).load(hotel.getHotelPicture()).into(viewHolder.productImage);
            } else {
                viewHolder.productImage.setImageBitmap(null);
            }

            if (!TextUtils.isEmpty(hotel.getHotelAddress())) {
                viewHolder.productLocation.setText(hotel.getHotelAddress());
            } else {
                viewHolder.productLocation.setText("Address unavailable");
            }

            if (!TextUtils.isEmpty(hotel.getHotelDescription())) {
                viewHolder.productDescription.setText(hotel.getHotelDescription());
            } else {
                viewHolder.productDescription.setText("Description unavailable");
            }

            viewHolder.bookAction2.setVisibility(View.GONE);
            viewHolder.bookAction3.setVisibility(View.GONE);
            viewHolder.viewAction2.setVisibility(View.GONE);
            viewHolder.viewAction3.setVisibility(View.GONE);

            viewHolder.bookAction1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hotel.setSessionId(model.getSessionId());
                    listener.onAction(context, "sendHotelDetails", null, hotel);
                }
            });
        } else if (payloadList != null) {
            final ALRichMessageModel.ALPayloadModel payload = payloadList.get(position);

            if (!TextUtils.isEmpty(payload.getHeaderImageUrl())) {
                Glide.with(context).load(payload.getHeaderImageUrl()).into(viewHolder.productImage);
                viewHolder.productImage.setVisibility(View.VISIBLE);
                viewHolder.productImageOverlay.setVisibility(View.VISIBLE);
                viewHolder.productPrice.setBackground(context.getResources().getDrawable(R.drawable.al_rich_messaging_price_border));
            } else {
                viewHolder.productImage.setVisibility(View.GONE);
                viewHolder.productImageOverlay.setVisibility(GONE);
                viewHolder.productPrice.setBackground(context.getResources().getDrawable(R.drawable.al_imageless_rich_message_price_border));
            }

            if (!TextUtils.isEmpty(payload.getOverlayText())) {
                viewHolder.productPrice.setText(payload.getOverlayText());
                viewHolder.productPrice.setVisibility(View.VISIBLE);
            } else {
                viewHolder.productPrice.setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(payload.getRating())) {
                viewHolder.productNameSplitLayout.setVisibility(GONE);
                if (!TextUtils.isEmpty(payload.getTitle())) {
                    viewHolder.productNameSingleLine.setVisibility(View.VISIBLE);
                    viewHolder.productNameSingleLine.setText(payload.getTitle());
                } else {
                    viewHolder.productNameSingleLine.setVisibility(View.GONE);
                }
            } else {
                viewHolder.productNameSplitLayout.setVisibility(View.VISIBLE);
                viewHolder.productNameSingleLine.setVisibility(View.GONE);
                viewHolder.productName.setVisibility(View.VISIBLE);
                viewHolder.productRating.setText(payload.getRating());

                if (!TextUtils.isEmpty(payload.getTitle())) {
                    viewHolder.productName.setText(payload.getTitle());
                } else {
                    viewHolder.productName.setText("");
                }
            }

            if (!TextUtils.isEmpty(payload.getSubtitle())) {
                viewHolder.productLocation.setVisibility(View.VISIBLE);
                viewHolder.productLocation.setText(payload.getSubtitle());
            } else {
                viewHolder.productLocation.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(payload.getDescription())) {
                viewHolder.productDescription.setVisibility(View.VISIBLE);
                viewHolder.productDescription.setText(payload.getDescription());
            } else {
                viewHolder.productDescription.setVisibility(View.GONE);
            }

            viewHolder.bookAction1.setVisibility(View.GONE);
            viewHolder.bookAction2.setVisibility(View.GONE);
            viewHolder.bookAction3.setVisibility(View.GONE);
            viewHolder.viewAction1.setVisibility(View.GONE);
            viewHolder.viewAction2.setVisibility(View.GONE);
            viewHolder.viewAction3.setVisibility(View.GONE);

            if (payload.getActions() != null && !payload.getActions().isEmpty()) {
                try {
                    List<ALRichMessageModel.AlActionModel> actionsList = payload.getActions();
                    //final List<ALRichMessageModel.AlActionModel> actionsList = Arrays.asList((ALRichMessageModel.AlActionModel[]) GsonUtils.getObjectFromJson(payload.getActions(), ALRichMessageModel.AlActionModel[].class));
                    for (int i = 0; i < actionsList.size(); i++) {
                        if (i == 0) {
                            viewHolder.bookAction1.setVisibility(View.VISIBLE);
                            viewHolder.viewAction1.setVisibility(View.VISIBLE);
                            viewHolder.bookAction1.setText(actionsList.get(0).getName());
                            viewHolder.bookAction1.setOnClickListener(getActionClickListener(actionsList.get(0)));
                        }

                        if (i == 1) {
                            viewHolder.bookAction2.setVisibility(View.VISIBLE);
                            viewHolder.viewAction2.setVisibility(View.VISIBLE);
                            viewHolder.bookAction2.setText(actionsList.get(1).getName());
                            viewHolder.bookAction2.setOnClickListener(getActionClickListener(actionsList.get(1)));
                        }

                        if (i == 2) {
                            viewHolder.bookAction3.setVisibility(View.VISIBLE);
                            viewHolder.viewAction3.setVisibility(View.VISIBLE);
                            viewHolder.bookAction3.setText(actionsList.get(2).getName());
                            viewHolder.bookAction3.setOnClickListener(getActionClickListener(actionsList.get(2)));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void bindSingleTextItem(SingleTextViewHolder holder, final int position) {
        if (model.getTemplateId() == 3) {
            if (payloadList.get(position).getName() != null) {
                holder.singleTextItem.setText(payloadList.get(position).getName().trim());
            }else{
                holder.singleTextItem.setText("");
            }
           /* ViewGroup.LayoutParams params = holder.rootLayout.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.rootLayout.setLayoutParams(params);*/
        } else {
            if (payloadList.get(position).getTitle() != null) {
                holder.singleTextItem.setText(payloadList.get(position).getTitle().trim());
            }else{
                holder.singleTextItem.setText("");
            }
        }
    }

    private void bindRoomDetailView(HotelDetailHolder holder, final int position) {

        final AlHotelBookingModel hotel = roomList.get(position);

        if (hotel != null) {
            if (!TextUtils.isEmpty(hotel.getRoomTypeName())) {
                holder.roomTypeTv.setText(hotel.getRoomTypeName());
            } else {
                holder.roomTypeTv.setText("Room name unavailable");
            }

            if (!TextUtils.isEmpty(hotel.getHotelPicture())) {
                Glide.with(context).load(hotel.getHotelPicture()).into(holder.productImage);
            } else {
                holder.productImage.setImageDrawable(null);
            }

            holder.noOfGuestTv.setText(String.valueOf(hotel.getNoOfGuest()));

            String text = "(1 Room for " + String.valueOf(hotel.getNoOfNights()) + " Nights)";
            holder.totalPriceHeaderTv.setText(text);

            holder.priceTv.setText(context.getString(R.string.rupee_symbol) + " " + String.valueOf(hotel.getPrice().getRoomPrice()));

            holder.totalPriceTv.setText(context.getString(R.string.rupee_symbol) + " " + String.valueOf(hotel.getPrice().getRoomPrice() * hotel.getNoOfNights()));

            holder.bookAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hotel.setSessionId(model.getSessionId());
                    listener.onAction(context, "sendRoomDetailsMessage", null, hotel);
                }
            });
        }
    }

    private void bindBookingDetailHolder(final BookingDetailsHolder holder, final int position) {
        final ALBookingDetailsModel detailsModel = new ALBookingDetailsModel();
        detailsModel.setSessionId(model.getSessionId());

        final ALBookingDetailsModel.ALBookingDetails bookingDetails = detailsModel.getPersonInfo();

        holder.titleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bookingDetails.setTitle(titleList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bookingDetails.setTitle("Title *");
            }
        });

        holder.submitAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(holder.firstNameEt.getText().toString().trim())
                        || TextUtils.isEmpty(holder.lastNameEt.getText().toString().trim())
                        || TextUtils.isEmpty(holder.emailIdEt.getText().toString().trim())
                        || TextUtils.isEmpty(holder.firstNameEt.getText().toString().trim())
                        || "Title *".equals(titleList.get(holder.titleSpinner.getSelectedItemPosition()))) {
                    Toast.makeText(context, "Mandatory fields required...", Toast.LENGTH_SHORT).show();
                } else {
                    bookingDetails.setTitle(titleList.get(holder.titleSpinner.getSelectedItemPosition()));
                    bookingDetails.setFirstName(holder.firstNameEt.getText().toString().trim());
                    bookingDetails.setLastName(holder.lastNameEt.getText().toString().trim());
                    bookingDetails.setEmailId(holder.emailIdEt.getText().toString().trim());
                    bookingDetails.setPhoneNo(holder.contactNumberEt.getText().toString().trim());

                    listener.onAction(context, "sendBookingDetails", null, detailsModel);
                }
            }
        });
    }

    private View.OnClickListener getActionClickListener(final ALRichMessageModel.AlActionModel action) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, action.getAction(), message, action.getData());
                }
                if (listener != null) {
                    listener.onAction(context, action.getAction(), message, action.getData());
                }
            }
        };
    }
}
