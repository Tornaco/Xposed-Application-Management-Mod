package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Sets;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/5/4 12:16.
 * God bless no bug!
 */
@Builder
@Getter
@AllArgsConstructor
@ToString
public class SystemProp implements Parcelable {

    public static final Set<String> FIELDS = Sets.newHashSet(
            Fields.DISPLAY,
            Fields.PRODUCT,
            Fields.DEVICE,
            Fields.BOARD,
            Fields.MANUFACTURER,
            Fields.BRAND,
            Fields.MODEL,
            Fields.HARDWARE
    );

    public interface Fields {
        String DISPLAY = "ro.build.display.id";
        String PRODUCT = "ro.product.name";
        String DEVICE = "ro.product.device";
        String BOARD = "ro.product.board";
        String MANUFACTURER = "ro.product.manufacturer";
        String BRAND = "ro.product.brand";
        String MODEL = "ro.product.model";
        String HARDWARE = "ro.hardware";
    }

    private SystemProp(Parcel in) {
        display = in.readString();
        product = in.readString();
        device = in.readString();
        board = in.readString();
        manufacturer = in.readString();
        brand = in.readString();
        model = in.readString();
        hardware = in.readString();
    }

    public static final Creator<SystemProp> CREATOR = new Creator<SystemProp>() {
        @Override
        public SystemProp createFromParcel(Parcel in) {
            return new SystemProp(in);
        }

        @Override
        public SystemProp[] newArray(int size) {
            return new SystemProp[size];
        }
    };

    public static String getProp(SystemProp prop, String propKey) {
        switch (propKey) {
            case Fields.BOARD:
                return prop.board;
            case Fields.BRAND:
                return prop.brand;
            case Fields.DEVICE:
                return prop.device;
            case Fields.DISPLAY:
                return prop.display;
            case Fields.HARDWARE:
                return prop.hardware;
            case Fields.MANUFACTURER:
                return prop.manufacturer;
            case Fields.MODEL:
                return prop.model;
            case Fields.PRODUCT:
                return prop.product;
            default:
                return null;
        }
    }

    private String display, product, device, board, manufacturer, brand, model, hardware;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(display);
        dest.writeString(product);
        dest.writeString(device);
        dest.writeString(board);
        dest.writeString(manufacturer);
        dest.writeString(brand);
        dest.writeString(model);
        dest.writeString(hardware);
    }
}
