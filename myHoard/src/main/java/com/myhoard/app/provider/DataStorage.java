package com.myhoard.app.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the data provider and application. Contains
 * definitions for the supported URIs and data columns.
 */
public final class DataStorage {
	/**
	 * This authority is used for writing to or querying from the database
	 * provider. Note: This is set at first run and cannot be changed without
	 * breaking apps that access the provider.
	 */
	public static final String AUTHORITY = "com.myhoard.app.provider";
	/**
	 * The content:// style URL for the top-level database authority
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * Columns from Collections table.
	 */
	protected interface CollectionsColumns {
        /**
         * Id on server. Column name.
         * <p>Type: TEXT</p>
         */
        public static final String ID_SERVER = "idServer";

		/**
		 * Name. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String NAME = "name";

		/**
		 * Description. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String DESCRIPTION = "description";

		/**
		 * Item picture filename. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String AVATAR_FILE_NAME = "avatarFileName";

		/**
		 * Tags. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String TAGS = "tags";

		/**
		 * Created date. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String CREATED_DATE = "createdDate";

		/**
		 * Modified date. Column name.
		 * <p>Type: NUMERIC</p>
		 */
		public static final String MODIFIED_DATE = "modifiedDate";

		/**
		 * URL to server. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String SERVERS = "servers";

        /**
         * Type of collection(offline,private,public). Column name.
         * <p>Type: NUMERIC</p>
         */
        public static final String TYPE = "type";

        /**
         * Number of items in collection. Column name.
         * <p>Type: NUMERIC</p>
         */
        public static final String ITEMS_NUMBER = "itemsNumber";

        /**
         * If object is synchronized with server. Column name.
         * <p>Type: BOOLEAN</p>
         */
        public static final String SYNCHRONIZED = "synchronized";

        /**
         * If object is deleted local. Column name.
         * <p>Type: BOOLEAN</p>
         */
        public static final String DELETED = "deleted";
	}

	protected interface ItemsColumns {
        /**
         * Id on server. Column name.
         * <p>Type: TEXT</p>
         */
        public static final String ID_SERVER = "idServer";

		/**
		 * Collection id to which the element belongs. Column name.
		 * <p>Type: INTEGER</p>
		 */
		public static final String ID_COLLECTION = "idCollection";

		/**
		 * Name. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String NAME = "name";

		/**
		 * Description. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String DESCRIPTION = "description";

		/**
		 * Location latitude. Column name.
		 * <p>Type: REAL</p>
		 */
		public static final String LOCATION_LAT = "locationLat";

		/**
		 * Location longitude. Column name.
		 * <p>Type: REAL</p>
		 */
		public static final String LOCATION_LNG = "locationLng";

        /**
         * Location. Column name.
         * <p>Type: TEXT</p>
         */
        public static final String LOCATION = "LOCATION";

		/**
		 * Created date. Column name.
		 * <p>Type: NUMERIC</p>
		 */
		public static final String CREATED_DATE = "createdDate";

		/**
		 * Modified date. Column name.
		 * <p>Type: NUMERIC</p>
		 */
		public static final String MODIFIED_DATE = "modifiedDate";

        /**
         * If object is synchronized with server. Column name.
         * <p>Type: BOOLEAN</p>
         */
        public static final String SYNCHRONIZED = "synchronized";

        /**
         * If object is deleted local. Column name.
         * <p>Type: BOOLEAN</p>
         */
        public static final String DELETED = "deleted";
	}

	protected interface MediaColumns {
        /**
         * Id on server. Column name.
         * <p>Type: TEXT</p>
         */
        public static final String ID_SERVER = "idServer";

		/**
		 * Item id to which the media belongs. Column name.
		 * <p>Type: INTEGER</p>
		 */
		public static final String ID_ITEM = "idItem";

		/**
		 * Name. Column name.
		 * <p>Type: BOOLEAN</p>
		 */
		public static final String AVATAR = "avatar";

		/**
		 * Media picture filename with full path. Column name.
		 * <p>Type: TEXT</p>
		 */
		public static final String FILE_NAME = "fileName";

		/**
		 * Created date of media. Column name.
		 * <p>Type: NUMERIC</p>
		 */
		public static final String CREATED_DATE = "createdDate";

        /**
         * If object is synchronized with server. Column name.
         * <p>Type: BOOLEAN</p>
         */
        public static final String SYNCHRONIZED = "synchronized";

        /**
         * If object is deleted local. Column name.
         * <p>Type: BOOLEAN</p>
         */
        public static final String DELETED = "deleted";
	}

    protected interface DeletedMediaColumns {
        /**
         * Id on server. Column name.
         * <p>Type: TEXT</p>
         */
        public static final String ID_SERVER = "idServer";
    }

	public static final class Collections implements BaseColumns, CollectionsColumns {
		public static final String TABLE_NAME = "collections";
		public static final String[] TABLE_COLUMNS = new String[]{
				_ID,
                ID_SERVER,
				NAME,
				DESCRIPTION,
				AVATAR_FILE_NAME,
				TAGS,
				CREATED_DATE,
				MODIFIED_DATE,
				SERVERS,
                TYPE,
                ITEMS_NUMBER,
                SYNCHRONIZED,
                DELETED
		};		public static final Uri CONTENT_URI = Uri.parse(DataStorage.CONTENT_URI + "/" + TABLE_NAME);
		/**
		 * This utility class cannot be instantiated
		 */
		private Collections() {}


	}

	public static final class Items implements BaseColumns, ItemsColumns {
		public static final String TABLE_NAME = "items";
		public static final String[] TABLE_COLUMNS = new String[]{
				_ID,
                ID_SERVER,
				ID_COLLECTION,
				NAME,
				DESCRIPTION,
				LOCATION_LAT,
				LOCATION_LNG,
                LOCATION,
				CREATED_DATE,
				MODIFIED_DATE,
                SYNCHRONIZED,
                DELETED
		};		public static final Uri CONTENT_URI = Uri.parse(DataStorage.CONTENT_URI + "/" + TABLE_NAME);

	}

	public static final class Media implements BaseColumns, MediaColumns {
		public static final String TABLE_NAME = "media";
		public static final String[] TABLE_COLUMNS = new String[]{
				_ID,
                ID_SERVER,
				ID_ITEM,
				AVATAR,
				FILE_NAME,
				CREATED_DATE,
                SYNCHRONIZED,
                DELETED
		};		public static final Uri CONTENT_URI = Uri.parse(DataStorage.CONTENT_URI + "/" + TABLE_NAME);

	}

    public static final class DeletedMedia implements BaseColumns, DeletedMediaColumns {
        public static final String TABLE_NAME = "deletedMedia";
        public static final String[] TABLE_COLUMNS = new String[]{
                _ID,
                ID_SERVER,
        };		public static final Uri CONTENT_URI = Uri.parse(DataStorage.CONTENT_URI + "/" + TABLE_NAME);
    }

    public enum TypeOfCollection {
        ERROR(0), OFFLINE(1), PUBLIC(2), PRIVATE(3);

        private int type;

        private TypeOfCollection (int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
