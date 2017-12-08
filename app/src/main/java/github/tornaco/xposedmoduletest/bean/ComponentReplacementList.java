package github.tornaco.xposedmoduletest.bean;

import com.google.gson.Gson;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@ToString
public class ComponentReplacementList {

    private List<ComponentReplacement> list;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static ComponentReplacementList fromJson(String json) {
        if (json == null) return null;
        try {
            return new Gson().fromJson(json.trim(), ComponentReplacementList.class);
        } catch (Throwable e) {
            return null;
        }
    }
}
