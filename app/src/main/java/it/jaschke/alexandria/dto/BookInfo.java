package it.jaschke.alexandria.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Created by RUPESH on 2/14/2016.
 */
@Data
public class BookInfo implements Serializable {
        private List<Items> items;
        private String totalItems;
        private String kind;

    }


