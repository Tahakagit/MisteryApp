package com.example.franc.misteryapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    TextView io;
    Realm mRealm = null;
    static RealmHelper helper = new RealmHelper();
    static Player  getPlayer = null;
    static MyListAdapter enemyAdapter;
    static MyWeaponsAdapter weaponsAdapter;

    //variabili per la generazione delle stringhe casuali
    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
    final java.util.Random rand = new java.util.Random();
    final Set<String> identifiers = new HashSet<String>();


    static RecyclerView list;
    static RecyclerView listWeapons;

    static ArrayList<Enemy> mEnemies = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mLayoutManagerWeapons;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        io = (TextView)findViewById(R.id.health);

        mEnemies = generateEnemies(2);
        enemyAdapter = new MyListAdapter(mEnemies);

        weaponsAdapter = new MyWeaponsAdapter(generateWeapons(4));

        //crea Realmobject Player se non esiste e ricarica energia
        isPlayer();

        startThreads(mEnemies);
        listWeapons = findViewById(R.id.rv_weapons);

        list = (RecyclerView)findViewById(R.id.rv);
        mLayoutManagerWeapons = new LinearLayoutManager(this);
        mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
        listWeapons.setLayoutManager(mLayoutManagerWeapons);
        listWeapons.setAdapter(weaponsAdapter);

        list.setAdapter(enemyAdapter);

    }

    // inizializza il Player
    public boolean isPlayer(){

        mRealm = Realm.getDefaultInstance();

        try {
            getPlayer = mRealm.where(Player.class).findAllAsync().first();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getPlayer == null){
            Player player = new Player();
            helper.addItem(player);
        }else {
            helper.restoreHealth(getPlayer);
        }
        mRealm.close();
        return true;
    }

    // riceve arraylist di Enemy e per ognuno avvia un thread
    public ArrayList<Enemy> startThreads(ArrayList<Enemy> enemies){
        for (Enemy res:enemies) {
            new BackgroundTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, res);

        }
        return enemies;
    }

    // genera N nemici
    public ArrayList<Enemy> generateEnemies(int enemiesNumber){
        ArrayList<Enemy> enemies = new ArrayList<Enemy>();

        for (int i = 0 ; i < enemiesNumber ; i++){
            Enemy enemy = new Enemy();
            enemy.setName(randomIdentifier());
            enemies.add(enemy);
        }
        return enemies;
    }

    // inserisce N armi e ne restituisce il RealmResult
    public RealmResults<WeaponSet> generateWeapons(int weaponsNumber){
//todo genera troppe armi
        RealmHelper helper = new RealmHelper();

        for (int i = 0 ; i < weaponsNumber ; i++){
            WeaponSet weapons = new WeaponSet();
            weapons.setWeaponName("Missile");
            helper.addWeapon(weapons);
        }

        return helper.getWeapons();
    }


    // genera stringhe casuali
    public String randomIdentifier() {
        StringBuilder builder = new StringBuilder();
        while(builder.toString().length() == 0) {
            int length = rand.nextInt(5)+5;
            for(int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if(identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }


    private class BackgroundTask extends AsyncTask<Enemy, Integer, Void>{
        public class Wrapper{

        ArrayList<Enemy> enemi;
        int salute;
    }

        void Sleep(int ms){
            try{
                Thread.sleep(ms);
            }
            catch (Exception e){
            }
        }

        @Override
        protected Void doInBackground(Enemy... arg0) {
            Wrapper wrapper = new Wrapper();
            final Player player;



            if (arg0[0].getHealth() >= 0) {
            Realm realm = Realm.getDefaultInstance();

            player = realm.where(Player.class).findFirst();
            while (player.getHealth() > 0) {
                Log.i("MainACtivity", arg0[0].getName() + " attacca");

                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            player.setHealth(player.getHealth() - 20);
                            realm.insertOrUpdate(player);
                            int salute = player.getHealth();

                            publishProgress(salute);

                        }
                    });
                } finally {
                }
                Sleep(2000);

            }

            if(realm != null) {
                realm.close();
            }

        }
/*
        Sleep(2000);
*/

/*
        wrapper.enemi = enemies;
        wrapper.salute = enemy.getHealth();
*/
        return null;
    }

        @Override
        protected void onProgressUpdate(Integer... values){
            io.setText(String.valueOf(values[0]));

        }



    }


}
