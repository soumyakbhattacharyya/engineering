package myst.maven.extensions.eventspy.broadcaster.internal;

import java.io.IOException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import myst.maven.extensions.eventspy.model.JobState;

public enum Format {
    JSON {
        private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().create();

        @Override
		public byte[] serialize(JobState jobState) throws IOException {
            return gson.toJson(jobState).getBytes( "UTF-8" );
        }
    };

    public abstract byte[] serialize(JobState jobState) throws IOException;
}
