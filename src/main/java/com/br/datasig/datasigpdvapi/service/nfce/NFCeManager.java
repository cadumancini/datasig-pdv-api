package com.br.datasig.datasigpdvapi.service.nfce;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NFCeManager {
    private static NFCeManager instance = null;
    private final List<String> nfcesInProgress;

    private NFCeManager() {
        nfcesInProgress = new ArrayList<>();
    }

    public static NFCeManager getInstance() {
        if (instance == null) {
            instance = new NFCeManager();
        }
        return instance;
    }

    public void addNFCE(String nfce) {
        nfcesInProgress.add(nfce);
    }

    public void removeNFCE(String nfce) {
        nfcesInProgress.remove(nfce);
    }

    public int getNumberOfNFCe() {
        return nfcesInProgress.size();
    }
}
