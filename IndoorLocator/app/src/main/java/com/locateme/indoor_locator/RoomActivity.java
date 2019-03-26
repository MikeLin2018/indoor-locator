package com.locateme.indoor_locator;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

public class RoomActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new RoomFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                int buildingID = getIntent().getExtras().getInt("building_id");
                Intent intent = new Intent(RoomActivity.this, NewRoomActivity.class);
                intent.putExtra("building_id", buildingID);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
