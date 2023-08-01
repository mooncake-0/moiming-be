package com.peoplein.moiming.service.util;


import java.util.List;

public interface FilePersistor {

    boolean persistFiles(List<TupleMultiPartFile> files);

}
