package com.example.explorethemet

import java.io.Serializable

class ArtObject(
    var objectId : String = "",
    var objectUrl : String = "",
    var accessionNumber : String = "",
    var department : String = "",
    var objectName : String = "",
    var title : String = "",
    var medium : String = "",
    var objectDate : String = "",
    var artistName : String = "",
    var artistBio : String = "",
    var creditLine : String = "",
    var image : String = ""
) : Serializable {
    override fun toString(): String {
        return "ArtObject(objectId=${objectId} objectUrl=${objectUrl} accessionNumber=${accessionNumber} department=${department} objectName=${objectName} title=${title} medium=${medium} objectDate=${objectDate} artistName=${artistName} artistBio=${artistBio} creditLine=${creditLine}"
    }

}
