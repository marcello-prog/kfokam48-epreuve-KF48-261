package cm.kfokam48.suivi.controller;

import cm.kfokam48.suivi.dto.TableauLigne;
import cm.kfokam48.suivi.service.TableauService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    @GetMapping
    public List<TableauLigne> tableau(@RequestParam Long promotionId) {
        return tableauService.genererTableau(promotionId);
    }
}
