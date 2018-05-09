package EmpiresMod.entities.Flags;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import EmpiresMod.API.Chat.IChatFormat;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.API.Chat.Component.ChatComponentList;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.API.JSON.API.SerializerTemplate;
import EmpiresMod.Localization.LocalizationManager;
import net.minecraft.util.IChatComponent;

public class Flag<T> implements Comparable<Flag>, IChatFormat {
	protected Gson gson = new GsonBuilder().create();

	public T value;
	public boolean configurable = true;
	public final FlagType<T> flagType;

	public Flag(FlagType<T> flagType, String serializedValue) {
		this.flagType = flagType;
		this.value = gson.fromJson(serializedValue, flagType.type);
	}

	public Flag(FlagType<T> flagType, T value) {
		this.flagType = flagType;
		this.value = value;
	}

	public boolean setValue(String value) {
		if (flagType.type == Boolean.class) {
			this.value = (T) Boolean.valueOf(value);
			return true;
		} else if (flagType.type == String.class) {
			this.value = (T) value;
			return true;
		} else if (flagType.type == Integer.class) {
			this.value = (T) Integer.valueOf(value);
			return true;
		} else if (flagType.type == Float.class) {
			this.value = (T) Float.valueOf(value);
			return true;
		}
		return false;
	}

	public boolean toggle() {
		if (flagType.type != Boolean.class) {
			return false;
		}
		this.value = (T) Boolean.valueOf(!((Boolean) this.value));
		return true;
	}

	@Override
	public String toString() {
		return toChatMessage().getUnformattedText();
	}

	@Override
	public int compareTo(Flag other) {
		return flagType.compareTo(other.flagType);
	}

	@Override
	public IChatComponent toChatMessage() {
		IChatComponent description = LocalizationManager.get(flagType.getDescriptionKey());
		return LocalizationManager.get("Empires.format.flag", flagType.name.toLowerCase(), description, value);
	}

	@SuppressWarnings("unchecked")
	public static class Serializer extends SerializerTemplate<Flag> {

		@Override
		public void register(GsonBuilder builder) {
			builder.registerTypeAdapter(Flag.class, this);
			new FlagType.Serializer().register(builder);
		}

		@Override
		public JsonElement serialize(Flag flag, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();

			json.add("flagType", context.serialize(flag.flagType));
			json.addProperty("value", flag.flagType.serializeValue(flag.value));

			return json;
		}

		@Override
		public Flag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();

			FlagType flagType = context.deserialize(jsonObject.get("flagType"), FlagType.class);
			Flag flag = new Flag(flagType, jsonObject.get("value").getAsString());

			return flag;
		}
	}

	public static class Container extends ArrayList<Flag> implements IChatFormat {

		public boolean contains(FlagType<?> flagType) {
			for (Flag flag : this) {
				if (flag.flagType == flagType) {
					return true;
				}
			}
			return false;
		}

		public <T> Flag<T> get(FlagType<T> flagType) {
			for (Flag flag : this) {
				if (flag.flagType == flagType) {
					return flag;
				}
			}
			return null;
		}

		public void remove(FlagType<?> flagType) {
			for (Iterator<Flag> it = iterator(); it.hasNext();) {
				if (it.next().flagType.equals(flagType)) {
					it.remove();
				}
			}
		}

		public <T> T getValue(FlagType<T> flagType) {
			for (Flag flag : this) {
				if (flag.flagType == flagType) {
					return (T) flag.value;
				}
			}
			return null;
		}

		@Override
		public IChatComponent toChatMessage() {
			IChatComponent root = new ChatComponentList();

			root.appendSibling(LocalizationManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|CURRENT FLAGS}")));
			//root.appendSibling(LocalizationManager.get("Empires.format.flags.linesecond", new ChatComponentFormatted("{9| CITIZENS - NEUTRALS - TRUCE - ALLIES - ENEMIES}")));
			//Use This When Relationships Work ^
			//Also need to make flags for each relationship^

			for (Flag flag : this) {
				root.appendSibling(flag.toChatMessage());
				
				
			}

			return root;
		}
	}
}