package com.example.immedsee.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.immedsee.R;
import com.example.immedsee.adapter.TargetAdapter;
import com.example.immedsee.entity.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * DoubleWay on 2019/2/15:16:30
 * 邮箱：13558965844@163.com
 */
public class FragmentTwo extends Fragment {
    //测试数据
    private Target[] targets={new Target("北京","我想知道北京长啥样"),new Target("南京","我想知道南京长啥样"),
            new Target("四川","我想知道四川长啥样")};

    private List<Target> targetList=new ArrayList<>();
    private TargetAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFruit();
    }

    @Override
    public void onStart() {
        super.onStart();
        RecyclerView recyclerView=getView().findViewById(R.id.recycle_view);
        GridLayoutManager layoutManager=new GridLayoutManager(getActivity().getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new TargetAdapter(targetList);
        recyclerView.setAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_two,container,false);
        return view;
    }

    private void initFruit() {
        targetList.clear();
        for(int i=0;i<50;i++){
            Random random=new Random();
            int index=random.nextInt(targets.length);
            targetList.add(targets[index]);
        }
    }
}
