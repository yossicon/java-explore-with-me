package ru.practicum.ewm.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationSaveDto;
import ru.practicum.ewm.compilation.dto.CompilationUpdateDto;
import ru.practicum.ewm.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid CompilationSaveDto compilationSaveDto) {
        log.info("Save compilation {}", compilationSaveDto);
        CompilationDto compilationDto = compilationService.addCompilation(compilationSaveDto);
        log.info("Compilation saved successfully, compilationDto: {}", compilationDto);
        return compilationDto;
    }

    @PatchMapping("/{compilationId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Long compilationId,
                                            @RequestBody @Valid CompilationUpdateDto compilationUpdateDto) {
        log.info("Update compilation {}", compilationUpdateDto);
        CompilationDto compilationDto = compilationService.updateCompilation(compilationId, compilationUpdateDto);
        log.info("Compilation updated successfully, compilationDto: {}", compilationDto);
        return compilationDto;
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compilationId) {
        log.info("Delete compilation with id {}", compilationId);
        compilationService.deleteCompilation(compilationId);
    }
}
