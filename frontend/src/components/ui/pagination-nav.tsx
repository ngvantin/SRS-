"use client"

import { Button } from "@/components/ui/button"
import { ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from "lucide-react"

interface PaginationNavProps {
    currentPage: number
    totalPages: number
    onPageChange: (page: number) => void
}

export function PaginationNav({ currentPage, totalPages, onPageChange }: PaginationNavProps) {
    return (
        <div className="flex items-center gap-1">
            <Button variant="outline" size="icon" onClick={() => onPageChange(1)} disabled={totalPages === 1 || currentPage === 1}>
                <ChevronsLeft className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="icon" onClick={() => onPageChange(currentPage - 1)} disabled={totalPages === 1 || currentPage === 1}>
                <ChevronLeft className="h-4 w-4" />
            </Button>

            <Button variant="outline" className="px-4">
                {currentPage}
            </Button>

            <Button
                variant="outline"
                size="icon"
                onClick={() => onPageChange(currentPage + 1)}
                disabled={totalPages === 1 || currentPage === totalPages}
            >
                <ChevronRight className="h-4 w-4" />
            </Button>
            <Button
                variant="outline"
                size="icon"
                onClick={() => onPageChange(totalPages)}
                disabled={totalPages === 1 || currentPage === totalPages}
            >
                <ChevronsRight className="h-4 w-4" />
            </Button>
        </div>
    )
}
