/*
 * Created by Storm Zhang, Apr 1, 2014.
 */

package com.storm.fliplayout;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nineoldandroids.view.ViewHelper;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.storm.fliplayout.R.id;
import com.storm.fliplayout.R.layout;
import com.storm.fliplayout.adapter.CommonAdapter;
import com.storm.fliplayout.bean.CommonViewHolder;
import com.storm.fliplayout.bean.PicWen;
import com.storm.fliplayout.communal.CommunityUrl;
import com.storm.fliplayout.dragLayout.DragLayout;
import com.storm.fliplayout.dragLayout.DragLayout.DragListener;
import com.storm.fliplayout.effect.PullListView;
import com.storm.fliplayout.effect.PullListView.OnRefreshListener;
import com.storm.fliplayout.effect.SearchListView;
import com.storm.fliplayout.helper.HttpHelper;
import com.storm.fliplayout.helper.ToastUtils;

public class MainActivity extends Activity {
	@ViewInject(R.id.left_drawer)
	ListView mDrawerList;
	@ViewInject(R.id.dl)
	DragLayout dragLayout;
	@ViewInject(R.id.main_title)
	ShimmerTextView main_title;
	@ViewInject(R.id.tv_set)
	TextView tv_set;
	@ViewInject(R.id.iv_icon)
	ImageView iv_icon;
	private PullListView mListView;

	private HttpUtils http;
	private Document mDocument;
	private List<PicWen> picWens;
	private List<String> titleData;
	private List<String> hrefData;
	private CommonAdapter<PicWen> commonAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.activity_main);
		ViewUtils.inject(this);
		init();
		initDragLayout();
		initView();
	}

	private void init() {
		http = new HttpUtils();
		picWens = new ArrayList<PicWen>();
		commonAdapter = new CommonAdapter<PicWen>(this, picWens,
				R.layout.listitem_flip) {

			@Override
			public void convert(CommonViewHolder helper, PicWen item) {
				TextView textView = helper.getView(R.id.title);
				textView.setText(item.getTitle());
				Glide.with(MainActivity.this).load(item.getImgUrl())
						.centerCrop()
						.placeholder(R.drawable.default_image_square)
						.crossFade()
						.into((ImageView) helper.getView(R.id.img_item));

			}

		};

	}

	private void initDragLayout() {
		dragLayout.setDragListener(new DragListener() {
			public void onOpen() {
				// mDrawerList.smoothScrollToPosition(new Random().nextInt(30));
			}

			public void onClose() {
				shake();
			}

			public void onDrag(float percent) {
				ViewHelper.setAlpha(iv_icon, 1 - percent);
			}
		});
	}

	private void shake() {
		iv_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
	}

	private void initView() {

		main_title = (ShimmerTextView) findViewById(R.id.main_title);
		Shimmer shimmer = new Shimmer();
		shimmer.start(main_title);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);

		iv_icon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				dragLayout.open();
			}
		});
		tv_set.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

			}
		});
		mListView = (PullListView) findViewById(id.listView);

		mListView.setAdapter(commonAdapter);
		mListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh(boolean isTop) {
				if (isTop) {
					loadFirstPageData();
				} else {

				}
			}
		});
		loadFirstPageData();

	}

	// 第一次加载数据
	private void loadFirstPageData() {

		mListView.state = SearchListView.REFRESHING;
		mListView.changeHeaderViewByState();
		RequestParams params = HttpHelper.getRequestParams(this);
		http.configTimeout(5000);
		http.configDefaultHttpCacheExpiry(0);// 配置默认的HTTP缓存到期
		http.send(HttpRequest.HttpMethod.GET, CommunityUrl.url,
				new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						ToastUtils.showShortToast(MainActivity.this,arg1);
						mListView.onRefreshComplete();
					}

					@Override
					public void onSuccess(ResponseInfo<String> response) {
						Log.i("hht","=="+response.result);
						mDocument = Jsoup.parse(response.result);
						
						titleData = new ArrayList<String>();
						Elements es = mDocument.getElementsByClass("xlistju");
						for (Element e : es) {
							titleData.add(e.text());
						}

						hrefData = new ArrayList<String>();
						Elements es1 = mDocument
								.getElementsByClass("chromeimg");
						for (Element e : es1) {
							hrefData.add(e.attr("src"));
						}

						for (int i = 0; i < hrefData.size(); i++) {
							PicWen picWen = new PicWen();
							picWen.setTitle(titleData.get(i));
							picWen.setImgUrl(hrefData.get(i));
							picWens.add(picWen);
						}
						mListView.onRefreshComplete();
						commonAdapter.notifyDataSetChanged();

					}
				});
	}

}
