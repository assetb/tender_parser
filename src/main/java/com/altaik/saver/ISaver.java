/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.saver;

/**
 *
 * @author admin
 */
public interface ISaver<T> {

    void Close();

    Query Do(T objectsForSave);
    
}
