/*
 * Copyright 2015 Arek Kita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

$(document).ready(function() {
    $('#upload').click(function (e) {
        e.preventDefault();
        var fileField = $('#fileId')[0];
        var fileName = fileField.files[0].name;
        var reader = new FileReader();
        reader.onload = function(event) {
            var targetRelativeUrl = '/' + fileName;
            $.ajax({
                url: targetRelativeUrl,
                type: 'PUT',
                success: function () {
                    $("#successUploadBlock").show();
                    $("#errorUploadBlock").hide();
                    $("#targetLink").attr("href", targetRelativeUrl);
                    $("#targetLink2").attr("href", targetRelativeUrl);
                    $("#delete").data("url", targetRelativeUrl);
                    $("#filename").text(fileName);
                    $("#filename2").text(fileName);
                    $("#deleteErrorMessage").text();
                },
                error: function () {
                    $("#successUploadBlock").hide();
                    $("#errorUploadBlock").show();
                    $("#deleteErrorMessage").text();
                },
                data: reader.result,
                cache: false,
                contentType: 'application/octet-stream',
                processData: false
            });
        };
        reader.readAsArrayBuffer(fileField.files[0]);
    });


    $('#delete').click(function (e) {
        e.preventDefault();
        $.ajax({
            url: $('#delete').data("url"),
            type: 'DELETE',
            success: function () {
                $("#deleteErrorMessage").removeClass("text-danger");
                $("#deleteErrorMessage").addClass("text-success");
                $("#deleteErrorMessage").text("DELETE was successful.");
                $("#deleteBlock").show();
            },
            error: function () {
                $("#deleteErrorMessage").removeClass("text-success");
                $("#deleteErrorMessage").addClass("text-danger");
                $("#deleteErrorMessage").text("Cannot delete file.");
            },
            cache: false,
            contentType: false,
            processData: false
        });
    })
});