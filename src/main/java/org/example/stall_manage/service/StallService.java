package org.example.stall_manage.service;

import org.example.stall_manage.pojo.Stall;

import java.util.List;
import java.util.Optional;

public interface StallService {
    List<Stall> find(Stall stall);

    void add(Stall stall);
}
