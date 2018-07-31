/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import java.util.List;

/**
 *
 * @author Vladimir
 */
public interface ISiteParser<T> {

    List<T> Proccess(int ipage);

    List<T> Do();

}
