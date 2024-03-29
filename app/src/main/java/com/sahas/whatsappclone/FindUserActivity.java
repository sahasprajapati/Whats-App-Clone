package com.sahas.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView nUserList;
    private RecyclerView.Adapter nUserListAdapter;
    private RecyclerView.LayoutManager nUserListLayoutManager;

    ArrayList<UserObject> userList, contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        contactList = new ArrayList<>();
        userList = new ArrayList<>();

        initializeRecyclerView();
        getContactList();
    }

    private void getContactList(){

        String ISOPrefix = getCountryISO();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null );
        while(phones.moveToNext()){
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(!String.valueOf(phone.charAt(0)).equals("+")){
                phone = ISOPrefix + phone;
            }
            UserObject nContact = new UserObject(name , phone);
            contactList.add(nContact);
            getUserDetails(nContact);
        }
    }

    private void getUserDetails(UserObject nContact) {
        DatabaseReference nUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = nUserDB.orderByChild("phone").equalTo(nContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String phone = "",
                            name = "";
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        if(childSnapshot.child("phone").getValue()!=null){
                            phone = childSnapshot.child("phone").getValue().toString();
                        }
                        if(childSnapshot.child("name").getValue()!=null){
                            phone = childSnapshot.child("name").getValue().toString();
                        }
                    }

                    UserObject nUser = new UserObject(name , phone);
                    userList.add(nUser);
                    nUserListAdapter.notifyDataSetChanged();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCountryISO(){
        String iso = null;
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso() != null ){
            if(telephonyManager.getNetworkCountryIso().toString().equals("")){
                iso = telephonyManager.getNetworkCountryIso().toString();
            }
        }
        return CountryToPhonePrefix.getPhone(iso);
    }
    private void initializeRecyclerView() {
        nUserList= findViewById(R.id.userList);
        nUserList.setNestedScrollingEnabled(false);
        nUserList.setHasFixedSize(false);
        nUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL,false);
        nUserList.setLayoutManager(nUserListLayoutManager);
        nUserListAdapter = new UserListAdapter(userList);
        nUserList.setAdapter(nUserListAdapter);
    }
}
