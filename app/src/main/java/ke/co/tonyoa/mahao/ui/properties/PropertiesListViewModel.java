package ke.co.tonyoa.mahao.ui.properties;

import static ke.co.tonyoa.mahao.ui.home.HomeViewModel.DEFAULT_COORDINATES;
import static ke.co.tonyoa.mahao.ui.home.HomeViewModel.DEFAULT_LIMIT;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import javax.inject.Inject;

import ke.co.tonyoa.mahao.app.MahaoApplication;
import ke.co.tonyoa.mahao.app.api.APIResponse;
import ke.co.tonyoa.mahao.app.api.responses.FavoriteResponse;
import ke.co.tonyoa.mahao.app.api.responses.Property;
import ke.co.tonyoa.mahao.app.enums.SortBy;
import ke.co.tonyoa.mahao.app.repositories.PropertiesRepository;

public class PropertiesListViewModel extends AndroidViewModel {

    @Inject
    PropertiesRepository mPropertiesRepository;
    private MutableLiveData<PropertiesListFragment.PropertyListType> mPropertyListMutableLiveData = new MutableLiveData<>();
    private LiveData<APIResponse<List<Property>>> mPropertiesLiveData;

    public PropertiesListViewModel(@NonNull Application application) {
        super(application);
        MahaoApplication.getInstance(application).getApplicationComponent().inject(this);
        mPropertyListMutableLiveData.postValue(PropertiesListFragment.PropertyListType.ALL);

        mPropertiesLiveData = Transformations.switchMap(mPropertyListMutableLiveData, input -> {
            switch (input){
                case ALL:
                    return mPropertiesRepository.getProperties(0, 2000, null, null, null,
                            null, null, null, null, null, null, null, null,
                            null, null, null, null);
                case LATEST:
                    return mPropertiesRepository.getLatestProperties(null, 0, 2000);
                case NEARBY:
                    return mPropertiesRepository.getProperties(0, DEFAULT_LIMIT, SortBy.DISTANCE, DEFAULT_COORDINATES, null, null,
                            null, null, null, null, null, DEFAULT_COORDINATES,
                            20, null, null, null, null);
                case POPULAR:
                    return mPropertiesRepository.getPopularProperties(null, 0, 2000);
                case FAVORITE:
                    return mPropertiesRepository.getFavoriteProperties(0, 2000);
                case PERSONAL:
                    return mPropertiesRepository.getUserProperties(0, 2000);
                case RECOMMENDED:
                    return mPropertiesRepository.getRecommendedProperties(null, 0, 2000);
            }
            return null;
        });

    }

    public void setPropertyList(PropertiesListFragment.PropertyListType propertyListType){
        mPropertyListMutableLiveData.postValue(propertyListType);
    }

    public LiveData<APIResponse<List<Property>>> getPropertiesLiveData(){
        return mPropertiesLiveData;
    }

    public LiveData<APIResponse<FavoriteResponse>> addFavorite(int propertyId) {
        return mPropertiesRepository.addFavorite(propertyId);
    }

    public LiveData<APIResponse<FavoriteResponse>> removeFavorite(int propertyId) {
        return mPropertiesRepository.removeFavorite(propertyId);
    }
}