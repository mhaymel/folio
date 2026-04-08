package com.folio.service;

import com.folio.dto.ImportResult;

@FunctionalInterface
interface Importer {
    ImportResult run(FileMultipartFile file) throws Exception;
}