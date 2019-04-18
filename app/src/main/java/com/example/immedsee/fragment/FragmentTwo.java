package com.example.immedsee.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.immedsee.R;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.activity.PostDetailsActivity;
import com.example.immedsee.adapter.PostListAdapter;
import com.example.immedsee.dao.Post;
import com.example.immedsee.dao.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;


/**
 * DoubleWay on 2019/2/15:16:30
 * 邮箱：13558965844@163.com
 */
public class FragmentTwo extends Fragment {
    public static final int REQUEST_CODE = 1;
     private FloatingActionButton addPost;
     private RecyclerView recyclerViewPost;
     private PostListAdapter postListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
     private PostAddDailogFragment postAddDailogFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_two,container,false);
        Log.d("fragmenttwo", "onCreateView: ");
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.post_toolBar);
        toolbar.setTitle("悬赏");
        addPost=(FloatingActionButton)view.findViewById(R.id.add_post);
        recyclerViewPost=(RecyclerView)view.findViewById(R.id.post_recycle_view);
        GridLayoutManager layoutManager=new GridLayoutManager(getActivity(),1);
        recyclerViewPost.setLayoutManager(layoutManager);


       /* User  user=BmobUser.getCurrentUser(User.class);*/
        UiTools.showSimpleLD(getContext(),R.string.loading);
        setPostInfo();
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* UiTools.showSimpleLD(getActivity(),R.string.postSend_loading);*/
                if(Constant.user!=null){
                    postAddDailogFragment=new PostAddDailogFragment();
                    postAddDailogFragment.setTargetFragment(FragmentTwo.this, REQUEST_CODE);
                    postAddDailogFragment.show(getFragmentManager(), "addPost");
                }else {
                    DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.please_login);
                    dialogPrompt.show();
                }

            }
        });
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);// 下拉刷新的逻辑
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setPostInfo();//重新从服务器上加载数据
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }
//有问题，每次重新加载的时候都会刷新的焦点都会重回第一项
    @Override
    public void onStart() {

        super.onStart();
        setPostInfo();
    }

    /**
     * 加载post帖子列表
     */
    private void setPostInfo() {

        BmobQuery<Post> query=new BmobQuery<>();
        /*query.addWhereEqualTo("deleteTag",0);*/
        query.include("author"); //查询包括他的作者信息
        query.findObjects(new FindListener<Post>() {
            @Override
            public void done(final List<Post> list, final BmobException e) {
                UiTools.closeSimpleLD();
                if(e==null){
                    Collections.reverse(list);//重要，将list的排列顺序倒置
                    //因为再外部进行条件查询好像有问题，查询总是失败，所以在查询后用一个list进行筛选
                    final List<Post>list2=new ArrayList<>();
                    for(Post post:list){
                        if(post.getDeleteTag()==0){
                            list2.add(post);
                        }
                    }
                   /* Log.d("fragmenttwo", "done: "+list.get(0).getAuthor().getObjectId());
                    Log.d("fragmenttwo", "done: "+list.get(0).getAuthor().getSignature());*/
                    postListAdapter=new PostListAdapter(list2);
                    recyclerViewPost.setAdapter(postListAdapter);
                    postListAdapter.notifyDataSetChanged();

                        postListAdapter.setOnItemClickListener(new PostListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if(Constant.user!=null) {
                                    Intent toPostDetailsIntent = new Intent(getContext(), PostDetailsActivity.class);
                                    toPostDetailsIntent.putExtra("post_data",list2.get(position));
                                    startActivity(toPostDetailsIntent);
                                }else {
                                    DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.please_login);
                                    dialogPrompt.show();
                                }
                            }
                        });
                }else {
                    Log.d("FragmentTwo", "done: 查询失败");
                    Log.d("FragmentTwo", "done: "+e.toString());
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE ){
            UiTools.closeSimpleLD();
            setPostInfo();
            postAddDailogFragment.dismiss();
        }
    }

}
