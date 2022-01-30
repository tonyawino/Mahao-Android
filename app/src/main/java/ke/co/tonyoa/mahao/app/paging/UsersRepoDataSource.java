package ke.co.tonyoa.mahao.app.paging;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ke.co.tonyoa.mahao.R;
import ke.co.tonyoa.mahao.app.api.APIResponse;
import ke.co.tonyoa.mahao.app.api.ApiManager;
import ke.co.tonyoa.mahao.app.api.responses.User;

public class UsersRepoDataSource extends RepoDataSource<User> {

    public UsersRepoDataSource(Context context, ApiManager apiManager) {
        super(context, apiManager);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params,
                            @NonNull final LoadInitialCallback<Integer, User> callback) {
        final int currentPage = 1;

        //setting load state so that the UI can know that progress of data fetching
        mLoadState.postValue(LOADING_ONGOING);

        //Simple retrofit call to get the first page of data
        try {
            int numPagesInitialLoad = 2;
            APIResponse<List<User>> listAPIResponse = mApiManager.getUsers(currentPage,
                    RepoDataSource.DEFAULT_PAGE_LIMIT* numPagesInitialLoad);
            if (listAPIResponse != null && listAPIResponse.isSuccessful()){
                //IMPORTANT: once the first page is load we increment the page count and pass it to callback.onResult()
                callback.onResult(listAPIResponse.body(),null,
                        currentPage+numPagesInitialLoad);
                //setting load state so that the UI can know data fetching is successful
                mLoadState.postValue(LOADING_SUCCESS);
            }
            else {
                //since no data was fetched we pass empty list and dont increment the page number
                //so that it can retry the fetching of 1st page
                callback.onResult(new ArrayList<>(), null, currentPage);

                //setting load state so that the UI can know data fetching failed
                mLoadState.postValue(LOADING_FAILED);

                mNetworkErrors.postValue(listAPIResponse!=null?listAPIResponse.errorMessage(mContext) :
                        mContext.getString(R.string.unknown_error));
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onResult(new ArrayList<>(), null, currentPage);
            mLoadState.postValue(LOADING_FAILED);
            mNetworkErrors.postValue(e.getMessage());
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, User> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, User> callback) {
        // we get the current page from params
        final int currentPage = params.key;

        mLoadState.postValue(LOADING_ONGOING);
        try {
            APIResponse<List<User>> listAPIResponse = mApiManager.getUsers(currentPage,
                    RepoDataSource.DEFAULT_PAGE_LIMIT);
            if (listAPIResponse != null && listAPIResponse.isSuccessful()){
                mLoadState.postValue(LOADING_SUCCESS);
                callback.onResult(listAPIResponse.body(), currentPage+1);
            }
            else {
                callback.onResult(new ArrayList<>(), currentPage);
                mLoadState.postValue(LOADING_FAILED);

                mNetworkErrors.postValue(listAPIResponse!=null?listAPIResponse.errorMessage(mContext) :
                        mContext.getString(R.string.unknown_error));
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onResult(new ArrayList<>(), currentPage);
            mLoadState.postValue(LOADING_FAILED);
            mNetworkErrors.postValue(e.getMessage());
        }
    }

    public static class UsersRepoDataSourceFactory extends Factory<Integer, User> {

        private final Context mContext;
        private final ApiManager mApiManager;
        private final MutableLiveData<UsersRepoDataSource> mUsersRepoDataSource;

        public UsersRepoDataSourceFactory(Context context, ApiManager apiManager) {
            mContext = context;
            mApiManager = apiManager;
            mUsersRepoDataSource = new MutableLiveData<>();
        }

        @NonNull
        @Override
        public DataSource<Integer, User> create() {
            UsersRepoDataSource collectorsRepoDataSource = new UsersRepoDataSource(mContext, mApiManager);
            mUsersRepoDataSource.postValue(collectorsRepoDataSource);
            return collectorsRepoDataSource;
        }

        public LiveData<UsersRepoDataSource> getUsersRepoDataSource() {
            return mUsersRepoDataSource;
        }
    }
}
