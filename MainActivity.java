package com.example.admin.musicplayer;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ListView list ;
    private double startTime = 0;
    private Handler mHandler = new Handler();
    SeekBar seekBar;
    TextView elapsed;
    final   MediaPlayer media=new MediaPlayer();
    static int count=0,current=0;
    Button next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.list);
        seekBar=findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        elapsed=findViewById(R.id.elapsed);
        next=findViewById(R.id.next);
        File file=new File(Environment.GetExternalstorage());
        file.mkdirs();
    }
    @Override
    protected void onResume() {
        super.onResume();
        File cache = new File(Environment.GetExternalstorage().getPath(), "list.txt");
        File fpath = new File(Environment.GetExternalstorage().getPath(), "fpath.txt");
        FileWriter file,path;
        if (!cache.exists()) {
            try {
                file = new FileWriter(cache, true);
                path=new FileWriter(fpath,true);
                findfiles(file,path);
                path.flush();
                path.close();
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                retrievefiles(cache,fpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void playMedia(final ArrayList<String> path,final ArrayList<String> file){

        TextView count=findViewById(R.id.count);
       final TextView time=findViewById(R.id.time);
        final TextView playing=findViewById(R.id.playing);
        count.setText("Songs:"+path.size());
        final Button play=findViewById(R.id.play);
        Button pause=findViewById(R.id.pause);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    media.reset();
                  try {
                    current=i;

                    media.setDataSource(path.get(i));

                    playing.setText("Playing:"+file.get(i));

                    media.prepare();
                                 int finalTime=media.getDuration();
                           time.setText(String.format("%02d :%02d ", TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                           TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                   finalTime)))
                      );
                           seekBar.setEnabled(true);
                           seekBar.setMax(finalTime);
                    media.start();
                    seekBar.setProgress((int)startTime);
                    mHandler.postDelayed(UpdateSongTime,100);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                media.start();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                media.pause();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                try {
                    if(current==path.size()-1)
                        current=0;
                    else
                        current++;
                    media.reset();
                    media.setDataSource(path.get(current));
                    media.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playing.setText("Playing:"+file.get(current));
                int finalTime=media.getDuration();
                time.setText(String.format("%02d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
                );
                seekBar.setEnabled(true);
                seekBar.setMax(finalTime);
                media.start();
                seekBar.setProgress((int)startTime);
                mHandler.postDelayed(UpdateSongTime,100);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(media != null && b){
                    media.seekTo(i );
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    private Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            startTime = media.getCurrentPosition();
           elapsed.setText(String.format("%02d :%02d ",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekBar.setProgress((int)startTime);
            mHandler.postDelayed(this, 100);
        }
    };
    public  void findfiles(FileWriter file,FileWriter fpath) throws IOException {
        ArrayList<String> files = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        //           ListView list = findViewById(R.id.list);
        display("sdcard", files, path,file,fpath);
        // String[] arr=files.toArray(new String[0]);
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        list.setAdapter(aa);
        playMedia(path,files);
    }
    public static void display(String str,ArrayList<String> files,ArrayList<String> path,FileWriter file,FileWriter fpath) throws IOException {
        String dir[] = listdir(str,files,path,file,fpath);
        for (String d : dir) {
            for (String s : listdir(d,files,path,file,fpath)) {
                display(s,files,path,file,fpath);
            }
        }
    }
    public static String[] listdir(String s, ArrayList<String> files, ArrayList<String> path, FileWriter filew, FileWriter fpath) throws IOException {
        ArrayList<String> ar = new ArrayList<>();
        File file = new File(s);
        File dir[] = file.listFiles();
        for (File f : dir) {
            if (!f.isDirectory()) {
                if(search(f.getName())) {
                    count++;
                    filew.append(count+" "+f.getName()+"\n");
                    fpath.append(f.getPath()+"\n");
                    files.add(count+" "+f.getName());
                    path.add(f.getPath());
                }

            } else {
                ar.add(f.toString());
            }
        }
        String di[] = ar.toArray(new String[0]);
        return di;
    }
    public  static boolean search(String file){
        String[] ext={".mp3",".ogg"};
        int last=file.lastIndexOf(".");
        if(last==-1)
            return false;
        for(String exten:ext){
            if(file.substring(last,file.length()).equals(exten)){
                return true;
            }
        }
        return false;
    }
    public void retrievefiles(File cache,File fpath) throws IOException {
        ArrayList<String> files = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        String current,curpath;
        FileReader reader=new FileReader(cache);
        BufferedReader breader=new BufferedReader(reader);
        FileReader preader=new FileReader(fpath);
        BufferedReader pathreader=new BufferedReader(preader);
        while((current=breader.readLine())!=null && (curpath=pathreader.readLine())!=null){
            files.add(current);
            path.add(curpath);
        }
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        list.setAdapter(aa);
        playMedia(path,files);
    }

}
